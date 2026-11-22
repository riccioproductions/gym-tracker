package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.AccountService;
import com.riccioproductions.gymtracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record Richiesta(String username, String password, String codiceInvito, String email) {}
    public record RichiestaReset(String email) {}
    public record ConfermaReset(String email, String codice, String nuovaPassword) {}
    public record Conferma(String password) {}
    public record CambioEmail(String email, String password) {}

    private final AuthService service;
    private final AccountService account;

    public AuthController(AuthService service, AccountService account) {
        this.service = service;
        this.account = account;
    }

    // Pubblico: la schermata di login lo usa per sapere se mostrare il campo "codice di invito"
    @GetMapping("/stato")
    public Map<String, Object> stato() {
        return Map.of("primoUtente", service.primoUtente());
    }

    @PostMapping("/registrati")
    public Map<String, String> registrati(@RequestBody Richiesta r, HttpServletRequest req) {
        return service.registra(r.username(), r.email(), r.password(), r.codiceInvito(), ip(req));
    }

    @PostMapping("/accedi")
    public Map<String, String> accedi(@RequestBody Richiesta r, HttpServletRequest req) {
        return service.accedi(r.username(), r.password(), ip(req));
    }

    // Pubblico, passo 1 del recupero: risponde sempre allo stesso modo, l'email esista o no
    @PostMapping("/recupera/richiedi")
    public Map<String, Boolean> richiediReset(@RequestBody RichiestaReset r, HttpServletRequest req) {
        service.richiediReset(r.email(), ip(req));
        return Map.of("ok", true);
    }

    // Pubblico, passo 2: email + codice ricevuto + nuova password. Restituisce il token (già collegato).
    @PostMapping("/recupera/conferma")
    public Map<String, String> confermaReset(@RequestBody ConfermaReset r, HttpServletRequest req) {
        return service.confermaReset(r.email(), r.codice(), r.nuovaPassword(), ip(req));
    }

    // Richiede il token: serve al frontend per verificare la sessione e mostrare l'email dell'account
    @GetMapping("/io")
    public Map<String, Object> io(@AuthenticationPrincipal Utente utente) {
        Map<String, Object> r = new HashMap<>(); // l'email può essere null: Map.of non la accetterebbe
        r.put("username", utente.getUsername());
        r.put("email", utente.getEmail());
        return r;
    }

    // Imposta o cambia l'email dell'account collegato; serve la password
    @PostMapping("/email")
    public void impostaEmail(@AuthenticationPrincipal Utente utente, @RequestBody CambioEmail c) {
        service.impostaEmail(utente.getId(), c.email(), c.password());
    }

    // Elimina l'account e tutti i suoi dati; serve la password. POST (e non DELETE) per portare il corpo senza problemi.
    @PostMapping("/account/elimina")
    public void eliminaAccount(@AuthenticationPrincipal Utente utente, @RequestBody Conferma c) {
        account.elimina(utente.getId(), c.password());
    }

    private static String ip(HttpServletRequest req) {
        String cf = req.getHeader("CF-Connecting-IP");
        if (cf != null && !cf.isBlank()) return cf.trim();
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] parti = xff.split(",");
            return parti[parti.length - 1].trim();
        }
        return req.getRemoteAddr();
    }
}
