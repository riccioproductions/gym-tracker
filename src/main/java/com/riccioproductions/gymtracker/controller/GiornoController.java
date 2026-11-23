package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.GiornoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schede/{schedaId}/giorni")
public class GiornoController {

    private final GiornoService service;

    public GiornoController(GiornoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Giorno> perScheda(@AuthenticationPrincipal Utente utente, @PathVariable Long schedaId) {
        return service.trovaPerScheda(schedaId, utente.getId());
    }

    @PostMapping
    public Giorno crea(@AuthenticationPrincipal Utente utente, @PathVariable Long schedaId,@Valid @RequestBody Giorno giorno) {
        giorno.setId(null);
        return service.salva(utente.getId(), schedaId, giorno);
    }

    @PutMapping("/{id}")
    public Giorno modifica(@AuthenticationPrincipal Utente utente, @PathVariable Long schedaId,
                           @PathVariable Long id,@Valid @RequestBody Giorno giorno) {
        giorno.setId(id);
        return service.salva(utente.getId(), schedaId, giorno);
    }

    @DeleteMapping("/{id}")
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        service.elimina(id, utente.getId());
    }
}
