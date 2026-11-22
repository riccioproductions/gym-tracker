package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.repository.AllenamentoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import com.riccioproductions.gymtracker.security.JwtService;
import com.riccioproductions.gymtracker.security.LimitatoreTentativi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_.-]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    // Alfabeto senza caratteri ambigui (niente 0/O, 1/I): 8 caratteri = 40 bit, valido 15 minuti, 5 tentativi
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int VALIDITA_MINUTI = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UtenteRepository utenti;
    private final SchedaRepository schede;
    private final EsercizioRepository esercizi;
    private final AllenamentoRepository allenamenti;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LimitatoreTentativi limitatore;
    private final MailService mail;
    private final String codiceInvito;

    public AuthService(UtenteRepository utenti, SchedaRepository schede, EsercizioRepository esercizi,
                       AllenamentoRepository allenamenti, PasswordEncoder encoder, JwtService jwt,
                       LimitatoreTentativi limitatore, MailService mail,
                       @Value("${gymtracker.codice-invito:}") String codiceInvito) {
        this.utenti = utenti;
        this.schede = schede;
        this.esercizi = esercizi;
        this.allenamenti = allenamenti;
        this.encoder = encoder;
        this.jwt = jwt;
        this.limitatore = limitatore;
        this.mail = mail;
        this.codiceInvito = codiceInvito == null ? "" : codiceInvito.trim();
    }

    /** true finché non esiste nessun utente: il primo che si registra eredita i dati già presenti. */
    public boolean primoUtente() {
        return utenti.count() == 0;
    }

    @Transactional
    public Map<String, String> registra(String username, String email, String password, String codice, String ip) {
        String nome = normalizza(username);
        if (!USERNAME.matcher(nome).matches()) {
            throw errore(HttpStatus.BAD_REQUEST,
                    "Username da 3 a 30 caratteri: lettere, numeri, punto, trattino e underscore.");
        }
        String mailNorm = controllaEmail(email);
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw errore(HttpStatus.BAD_REQUEST, "La password deve avere da 8 a 72 caratteri.");
        }

        boolean primo = primoUtente();
        if (!primo) {
            if (codiceInvito.isEmpty()) {
                throw errore(HttpStatus.FORBIDDEN, "Le registrazioni sono chiuse.");
            }
            limitatore.verifica("ip:" + ip, 20);
            if (!uguali(codiceInvito, codice == null ? "" : codice.trim())) {
                limitatore.fallito("ip:" + ip);
                throw errore(HttpStatus.FORBIDDEN, "Codice di invito non valido.");
            }
        }
        if (utenti.existsByUsername(nome)) {
            throw errore(HttpStatus.CONFLICT, "Username già in uso.");
        }
        if (utenti.existsByEmail(mailNorm)) {
            throw errore(HttpStatus.CONFLICT, "Email già in uso.");
        }

        Utente nuovo = new Utente();
        nuovo.setUsername(nome);
        nuovo.setEmail(mailNorm);
        nuovo.setPasswordHash(encoder.encode(password));
        nuovo = utenti.save(nuovo);

        if (primo) { // migrazione: schede, esercizi e allenamenti creati prima del multiutente
            schede.assegnaOrfane(nuovo);
            esercizi.assegnaOrfani(nuovo);
            allenamenti.assegnaOrfani(nuovo);
        }

        // Catalogo iniziale: chi parte da zero trova già gli esercizi più comuni
        if (esercizi.findByUtenteId(nuovo.getId()).isEmpty()) {
            esercizi.saveAll(CatalogoEsercizi.per(nuovo));
        }
        return risposta(nuovo);
    }

    public Map<String, String> accedi(String username, String password, String ip) {
        String nome = normalizza(username);
        limitatore.verifica("ip:" + ip, 20);
        limitatore.verifica("utente:" + nome, 5);

        Utente u = utenti.findByUsername(nome).orElse(null);
        if (u == null || password == null || !encoder.matches(password, u.getPasswordHash())) {
            limitatore.fallito("ip:" + ip);
            limitatore.fallito("utente:" + nome);
            throw errore(HttpStatus.UNAUTHORIZED, "Username o password errati.");
        }
        limitatore.azzera("utente:" + nome);
        return risposta(u);
    }

    /** Password dimenticata, passo 1: invia per email un codice monouso. Risponde sempre allo stesso modo (non rivela se l'email esiste). */
    @Transactional
    public void richiediReset(String email, String ip) {
        String e = email == null ? "" : email.trim().toLowerCase();
        limitatore.verifica("resetip:" + ip, 10);
        limitatore.verifica("resetmail:" + e, 3); // al massimo 3 email ogni 15 minuti per indirizzo
        limitatore.fallito("resetip:" + ip);
        limitatore.fallito("resetmail:" + e);

        Utente u = utenti.findByEmail(e).orElse(null);
        if (u == null || !mail.attivo()) return;

        String codice = nuovoCodice();
        u.setResetCodiceHash(encoder.encode(normalizzaCodice(codice)));
        u.setResetScadenza(LocalDateTime.now().plusMinutes(VALIDITA_MINUTI));
        utenti.save(u);
        mail.invia(u.getEmail(), "GymTracker: codice per reimpostare la password",
                "Ciao " + u.getUsername() + ",\n\n"
                + "il tuo codice per reimpostare la password è:\n\n    " + codice + "\n\n"
                + "Vale " + VALIDITA_MINUTI + " minuti e si può usare una sola volta.\n"
                + "Se non l'hai richiesto tu, ignora questa email: la tua password non è cambiata.\n");
    }

    /** Passo 2: email + codice + nuova password. In caso di successo l'utente è già collegato. */
    @Transactional
    public Map<String, String> confermaReset(String email, String codice, String nuovaPassword, String ip) {
        String e = email == null ? "" : email.trim().toLowerCase();
        limitatore.verifica("resetip:" + ip, 10);
        limitatore.verifica("resetcodice:" + e, 5);
        if (nuovaPassword == null || nuovaPassword.length() < 8 || nuovaPassword.length() > 72) {
            throw errore(HttpStatus.BAD_REQUEST, "La password deve avere da 8 a 72 caratteri.");
        }

        Utente u = utenti.findByEmail(e).orElse(null);
        boolean valido = u != null && u.getResetCodiceHash() != null && u.getResetScadenza() != null
                && u.getResetScadenza().isAfter(LocalDateTime.now())
                && encoder.matches(normalizzaCodice(codice), u.getResetCodiceHash());
        if (!valido) {
            limitatore.fallito("resetip:" + ip);
            limitatore.fallito("resetcodice:" + e);
            throw errore(HttpStatus.UNAUTHORIZED, "Email o codice errati, oppure codice scaduto.");
        }
        limitatore.azzera("resetcodice:" + e);
        limitatore.azzera("utente:" + u.getUsername());

        u.setPasswordHash(encoder.encode(nuovaPassword));
        u.setResetCodiceHash(null); // monouso
        u.setResetScadenza(null);
        utenti.save(u);
        return risposta(u);
    }

    /** Utente collegato: imposta o cambia la propria email. Serve la password. */
    @Transactional
    public void impostaEmail(Long utenteId, String email, String password) {
        Utente u = utenti.findById(utenteId)
                .orElseThrow(() -> errore(HttpStatus.NOT_FOUND, "Utente non trovato."));
        limitatore.verifica("utente:" + u.getUsername(), 5);
        // 403 e non 401: un 401 farebbe uscire l'utente dalla sessione
        if (password == null || !encoder.matches(password, u.getPasswordHash())) {
            limitatore.fallito("utente:" + u.getUsername());
            throw errore(HttpStatus.FORBIDDEN, "Password errata.");
        }
        limitatore.azzera("utente:" + u.getUsername());

        String nuova = controllaEmail(email);
        utenti.findByEmail(nuova).ifPresent(altro -> {
            if (!altro.getId().equals(u.getId())) throw errore(HttpStatus.CONFLICT, "Email già in uso.");
        });
        u.setEmail(nuova);
        u.setResetCodiceHash(null); // un codice emesso per il vecchio indirizzo non vale più
        u.setResetScadenza(null);
        utenti.save(u);
    }

    private static String controllaEmail(String email) {
        String e = email == null ? "" : email.trim().toLowerCase();
        if (e.length() > 254 || !EMAIL.matcher(e).matches()) {
            throw errore(HttpStatus.BAD_REQUEST, "Inserisci un indirizzo email valido.");
        }
        return e;
    }

    // Codice nel formato XXXX-XXXX
    private static String nuovoCodice() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append('-');
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    private static String normalizzaCodice(String codice) {
        return codice == null ? "" : codice.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private Map<String, String> risposta(Utente u) {
        return Map.of("token", jwt.genera(u), "username", u.getUsername());
    }

    private static String normalizza(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    // Confronto a tempo costante, per non rivelare il codice a tentativi
    private static boolean uguali(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseStatusException errore(HttpStatus stato, String messaggio) {
        return new ResponseStatusException(stato, messaggio);
    }
}
