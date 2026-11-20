package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record Richiesta(String username, String password, String codiceInvito) {}

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // Pubblico: la schermata di login lo usa per sapere se mostrare il campo "codice di invito"
    @GetMapping("/stato")
    public Map<String, Object> stato() {
        return Map.of("primoUtente", service.primoUtente());
    }

    @PostMapping("/registrati")
    public Map<String, String> registrati(@RequestBody Richiesta r, HttpServletRequest req) {
        return service.registra(r.username(), r.password(), r.codiceInvito(), ip(req));
    }

    @PostMapping("/accedi")
    public Map<String, String> accedi(@RequestBody Richiesta r, HttpServletRequest req) {
        return service.accedi(r.username(), r.password(), ip(req));
    }

    // Richiede il token: serve al frontend per verificare la sessione
    @GetMapping("/io")
    public Map<String, Object> io(@AuthenticationPrincipal Utente utente) {
        return Map.of("username", utente.getUsername());
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
