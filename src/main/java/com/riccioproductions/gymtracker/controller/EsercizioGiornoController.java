package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import com.riccioproductions.gymtracker.service.EsercizioGiornoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/giorni/{giornoId}/esercizi")
@CrossOrigin(origins = "*")
public class EsercizioGiornoController {

    private final EsercizioGiornoService service;

    public EsercizioGiornoController(EsercizioGiornoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EsercizioGiorno> perGiorno(@PathVariable Long giornoId) {
        return service.trovaPerGiorno(giornoId);
    }

    @PostMapping("/{esercizioId}")
    public EsercizioGiorno crea(@PathVariable Long giornoId, @PathVariable Long esercizioId,
                                 @RequestBody EsercizioGiorno eg) {
        return service.salva(giornoId, esercizioId, eg);
    }

    @PutMapping("/{id}/{esercizioId}")
    public EsercizioGiorno modifica(@PathVariable Long giornoId, @PathVariable Long id,
                                     @PathVariable Long esercizioId, @RequestBody EsercizioGiorno eg) {
        eg.setId(id);
        return service.salva(giornoId, esercizioId, eg);
    }

    @DeleteMapping("/{id}")
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }
}