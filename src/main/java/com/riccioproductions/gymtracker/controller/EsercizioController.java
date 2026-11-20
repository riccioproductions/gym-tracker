package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.EsercizioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/esercizi")
public class EsercizioController {

    private final EsercizioService service;

    public EsercizioController(EsercizioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Esercizio> tutti(@AuthenticationPrincipal Utente utente) {
        return service.trovaTutti(utente.getId());
    }

    @PostMapping
    public Esercizio crea(@AuthenticationPrincipal Utente utente, @RequestBody Esercizio esercizio) {
        esercizio.setId(null);
        return service.salva(utente.getId(), esercizio);
    }

    @PutMapping("/{id}")
    public Esercizio modifica(@AuthenticationPrincipal Utente utente, @PathVariable Long id,
                              @RequestBody Esercizio esercizio) {
        esercizio.setId(id);
        return service.salva(utente.getId(), esercizio);
    }

    @DeleteMapping("/{id}")
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        service.elimina(id, utente.getId());
    }
}
