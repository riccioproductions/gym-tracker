package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.PushSottoscrizione;
import com.riccioproductions.gymtracker.repository.PushSottoscrizioneRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// Notifica di fine recupero anche a schermo spento (Web Push).
// Il frontend dice "tra N secondi" e il server invia la push ai dispositivi dell'utente.
// Le programmazioni stanno in memoria: un riavvio del server le azzera (basta e avanza per recuperi di pochi minuti).
// Senza chiavi VAPID configurate il servizio resta spento e l'app funziona come prima.
@Service
public class NotificheService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificheService.class);

    // Il server contatta l'indirizzo scelto dal client: ammettiamo solo i servizi push dei browser noti
    private static final List<String> HOST_AMMESSI = List.of(
            "fcm.googleapis.com", "push.services.mozilla.com", "push.apple.com", "notify.windows.com");
    private static final String PAYLOAD = "{\"titolo\":\"Recupero terminato\",\"corpo\":\"Puoi iniziare la serie successiva.\"}";

    private final PushSottoscrizioneRepository repository;
    private final UtenteRepository utenti;
    private final PushService push; // null se le chiavi non sono configurate
    private final String chiavePubblica;
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
    private final Map<Long, ScheduledFuture<?>> programmati = new ConcurrentHashMap<>();

    public NotificheService(PushSottoscrizioneRepository repository, UtenteRepository utenti,
                            @Value("${gymtracker.push.chiave-pubblica:}") String pubblica,
                            @Value("${gymtracker.push.chiave-privata:}") String privata,
                            @Value("${gymtracker.push.soggetto:mailto:admin@example.com}") String soggetto)
            throws GeneralSecurityException {
        this.repository = repository;
        this.utenti = utenti;
        this.chiavePubblica = pubblica == null ? "" : pubblica.trim();
        if (this.chiavePubblica.isEmpty() || privata == null || privata.isBlank()) {
            this.push = null;
            LOG.warn("Notifiche push NON configurate: mancano le chiavi VAPID (GYM_PUSH_PUBBLICA / GYM_PUSH_PRIVATA)");
        } else {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            this.push = new PushService(this.chiavePubblica, privata.trim(), soggetto);
        }
    }

    /** Chiave pubblica VAPID per il browser; vuota se le push non sono configurate. */
    public String chiavePubblica() {
        return push == null ? "" : chiavePubblica;
    }

    @Transactional
    public void salva(Long utenteId, String endpoint, String p256dh, String auth) {
        controllaEndpoint(endpoint);
        if (p256dh == null || p256dh.isBlank() || p256dh.length() > 200
                || auth == null || auth.isBlank() || auth.length() > 100) {
            throw new RuntimeException("Sottoscrizione non valida");
        }
        // Stesso dispositivo: l'ultimo utente che lo registra lo "possiede" (utile se due persone usano lo stesso telefono)
        PushSottoscrizione s = repository.findByEndpoint(endpoint).orElseGet(PushSottoscrizione::new);
        s.setUtente(utenti.getReferenceById(utenteId));
        s.setEndpoint(endpoint);
        s.setP256dh(p256dh);
        s.setAuth(auth);
        repository.save(s);
    }

    @Transactional
    public void rimuovi(Long utenteId, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return;
        repository.deleteByEndpointAndUtenteId(endpoint, utenteId);
    }

    /** Invia la notifica tra `secondi`; sostituisce un invio già programmato per lo stesso utente. */
    public void programma(Long utenteId, int secondi) {
        if (push == null) return;
        annulla(utenteId);
        int s = Math.max(1, Math.min(secondi, 1800));
        programmati.put(utenteId, pool.schedule(() -> {
            programmati.remove(utenteId);
            invia(utenteId);
        }, s, TimeUnit.SECONDS));
    }

    public void annulla(Long utenteId) {
        ScheduledFuture<?> f = programmati.remove(utenteId);
        if (f != null) f.cancel(false);
    }

    private void invia(Long utenteId) {
        for (PushSottoscrizione s : repository.findByUtenteId(utenteId)) {
            try {
                HttpResponse r = push.send(new Notification(s.getEndpoint(), s.getP256dh(), s.getAuth(), PAYLOAD));
                int stato = r.getStatusLine().getStatusCode();
                LOG.info("Push inviata (utente {}): risposta del servizio {}", utenteId, stato);
                if (stato == 404 || stato == 410) repository.delete(s); // dispositivo non più valido
            } catch (Exception e) {
                // un dispositivo che non risponde non deve bloccare gli altri
                LOG.warn("Invio push fallito (utente {}): {}", utenteId, e.toString());
            }
        }
    }

    /** Diagnostica per l'utente: com'è messo il server e quanti dispositivi ha; se tutto ok, invia una push di prova tra 10 secondi. */
    public Map<String, Object> prova(Long utenteId) {
        int dispositivi = repository.findByUtenteId(utenteId).size();
        boolean configurato = push != null;
        if (configurato && dispositivi > 0) programma(utenteId, 10);
        return Map.<String, Object>of("configurato", configurato, "dispositivi", dispositivi);
    }

    private static void controllaEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank() || endpoint.length() > 600) {
            throw new RuntimeException("Sottoscrizione non valida");
        }
        String host;
        String schema;
        try {
            URI u = URI.create(endpoint);
            host = u.getHost() == null ? "" : u.getHost().toLowerCase();
            schema = u.getScheme();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Sottoscrizione non valida");
        }
        boolean ok = "https".equals(schema)
                && HOST_AMMESSI.stream().anyMatch(a -> host.equals(a) || host.endsWith("." + a));
        if (!ok) throw new RuntimeException("Servizio di notifica non supportato");
    }
}
