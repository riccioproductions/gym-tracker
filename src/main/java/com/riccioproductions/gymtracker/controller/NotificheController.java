package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.NotificheService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class NotificheController {

    public record Sottoscrizione(String endpoint, String p256dh, String auth) {}
    public record Dispositivo(String endpoint) {}
    public record Recupero(int secondi) {}

    private final NotificheService service;

    public NotificheController(NotificheService service) {
        this.service = service;
    }

    // Chiave pubblica VAPID: serve al browser per registrarsi. Vuota = push non configurate sul server.
    @GetMapping("/chiave")
    public Map<String, String> chiave() {
        return Map.of("chiave", service.chiavePubblica());
    }

    @PostMapping("/sottoscrizione")
    public void sottoscrivi(@AuthenticationPrincipal Utente utente, @RequestBody Sottoscrizione s) {
        service.salva(utente.getId(), s.endpoint(), s.p256dh(), s.auth());
    }

    @DeleteMapping("/sottoscrizione")
    public void rimuovi(@AuthenticationPrincipal Utente utente, @RequestBody Dispositivo d) {
        service.rimuovi(utente.getId(), d.endpoint());
    }

    // Programma (o riprogramma) la notifica di fine recupero tra N secondi
    @PostMapping("/recupero")
    public void programma(@AuthenticationPrincipal Utente utente, @RequestBody Recupero r) {
        service.programma(utente.getId(), r.secondi());
    }

    // Diagnostica + push di prova tra 10 secondi
    @PostMapping("/prova")
    public Map<String, Object> prova(@AuthenticationPrincipal Utente utente) {
        return service.prova(utente.getId());
    }

    @DeleteMapping("/recupero")
    public void annulla(@AuthenticationPrincipal Utente utente) {
        service.annulla(utente.getId());
    }
}
