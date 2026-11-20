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
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_.-]{3,30}$");

    private final UtenteRepository utenti;
    private final SchedaRepository schede;
    private final EsercizioRepository esercizi;
    private final AllenamentoRepository allenamenti;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LimitatoreTentativi limitatore;
    private final String codiceInvito;

    public AuthService(UtenteRepository utenti, SchedaRepository schede, EsercizioRepository esercizi,
                       AllenamentoRepository allenamenti, PasswordEncoder encoder, JwtService jwt,
                       LimitatoreTentativi limitatore,
                       @Value("${gymtracker.codice-invito:}") String codiceInvito) {
        this.utenti = utenti;
        this.schede = schede;
        this.esercizi = esercizi;
        this.allenamenti = allenamenti;
        this.encoder = encoder;
        this.jwt = jwt;
        this.limitatore = limitatore;
        this.codiceInvito = codiceInvito == null ? "" : codiceInvito.trim();
    }

    /** true finché non esiste nessun utente: il primo che si registra eredita i dati già presenti. */
    public boolean primoUtente() {
        return utenti.count() == 0;
    }

    @Transactional
    public Map<String, String> registra(String username, String password, String codice, String ip) {
        String nome = normalizza(username);
        if (!USERNAME.matcher(nome).matches()) {
            throw errore(HttpStatus.BAD_REQUEST,
                    "Username da 3 a 30 caratteri: lettere, numeri, punto, trattino e underscore.");
        }
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

        Utente nuovo = new Utente();
        nuovo.setUsername(nome);
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
