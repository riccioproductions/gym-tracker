package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.repository.AllenamentoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.PushSottoscrizioneRepository;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import com.riccioproductions.gymtracker.security.LimitatoreTentativi;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Eliminazione definitiva dell'account e di TUTTI i dati dell'utente.
@Service
public class AccountService {

    private final UtenteRepository utenti;
    private final SchedaRepository schede;
    private final EsercizioRepository esercizi;
    private final AllenamentoRepository allenamenti;
    private final PushSottoscrizioneRepository push;
    private final NotificheService notifiche;
    private final PasswordEncoder encoder;
    private final LimitatoreTentativi limitatore;

    public AccountService(UtenteRepository utenti, SchedaRepository schede, EsercizioRepository esercizi,
                          AllenamentoRepository allenamenti, PushSottoscrizioneRepository push,
                          NotificheService notifiche, PasswordEncoder encoder, LimitatoreTentativi limitatore) {
        this.utenti = utenti;
        this.schede = schede;
        this.esercizi = esercizi;
        this.allenamenti = allenamenti;
        this.push = push;
        this.notifiche = notifiche;
        this.encoder = encoder;
        this.limitatore = limitatore;
    }

    @Transactional
    public void elimina(Long utenteId, String password) {
        Utente u = utenti.findById(utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato."));
        limitatore.verifica("utente:" + u.getUsername(), 5);
        // 403 e non 401: un 401 farebbe credere al frontend che la sessione sia scaduta
        if (password == null || !encoder.matches(password, u.getPasswordHash())) {
            limitatore.fallito("utente:" + u.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password errata.");
        }
        limitatore.azzera("utente:" + u.getUsername());

        notifiche.annulla(utenteId);
        svuotaDati(utenteId);
        push.deleteAll(push.findByUtenteId(utenteId));
        push.flush();
        utenti.delete(u);
    }

    /** Cancella schede, esercizi e allenamenti dell'utente (non l'account). Usata anche dall'import. */
    @Transactional
    public void svuotaDati(Long utenteId) {
        // L'ordine conta per le chiavi esterne: prima allenamenti (con le serie), poi schede
        // (con giorni ed esercizi del giorno), infine il catalogo esercizi.
        allenamenti.deleteAll(allenamenti.findByUtenteId(utenteId, Sort.unsorted()));
        allenamenti.flush();
        schede.deleteAll(schede.findByUtenteIdOrderByIdAsc(utenteId));
        schede.flush();
        esercizi.deleteAll(esercizi.findByUtenteId(utenteId));
        esercizi.flush();
    }
}
