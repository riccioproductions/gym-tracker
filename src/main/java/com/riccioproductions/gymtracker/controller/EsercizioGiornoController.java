package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.EsercizioGiornoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/giorni/{giornoId}/esercizi")
public class EsercizioGiornoController {

    private final EsercizioGiornoService service;

    public EsercizioGiornoController(EsercizioGiornoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EsercizioGiorno> perGiorno(@AuthenticationPrincipal Utente utente, @PathVariable Long giornoId) {
        return service.trovaPerGiorno(giornoId, utente.getId());
    }

    @PostMapping("/{esercizioId}")
    public EsercizioGiorno crea(@AuthenticationPrincipal Utente utente, @PathVariable Long giornoId,
                                @PathVariable Long esercizioId, @Valid @RequestBody EsercizioGiorno eg) {
        eg.setId(null); 
        return service.salva(utente.getId(), giornoId, esercizioId, eg);
    }

    @PutMapping("/{id}/{esercizioId}")
    public EsercizioGiorno modifica(@AuthenticationPrincipal Utente utente, @PathVariable Long giornoId, @PathVariable Long id, @PathVariable Long esercizioId,@Valid @RequestBody EsercizioGiorno eg) {
        eg.setId(id);
        return service.salva(utente.getId(), giornoId, esercizioId, eg);
    }

    @DeleteMapping("/{id}")
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        service.elimina(id, utente.getId());
    }
}
