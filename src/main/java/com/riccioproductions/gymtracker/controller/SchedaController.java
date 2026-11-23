package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.SchedaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schede")
public class SchedaController {

    private final SchedaService service;

    public SchedaController(SchedaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Scheda> tutte(@AuthenticationPrincipal Utente utente) {
        return service.trovaTutte(utente.getId());
    }

    @GetMapping("/{id}")
    public Scheda unaSingola(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        return service.trovaPerId(id, utente.getId());
    }

    @PostMapping
    public Scheda crea(@AuthenticationPrincipal Utente utente,@Valid @RequestBody Scheda scheda) {
        scheda.setId(null); 
        return service.salva(utente.getId(), scheda);
    }

    @PutMapping("/{id}")
    public Scheda modifica(@AuthenticationPrincipal Utente utente, @PathVariable Long id, @Valid @RequestBody Scheda scheda) {
        scheda.setId(id);
        return service.salva(utente.getId(), scheda);
    }

    @DeleteMapping("/{id}")
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        service.elimina(id, utente.getId());
    }
}
