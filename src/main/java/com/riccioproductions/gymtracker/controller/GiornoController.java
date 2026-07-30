package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.service.GiornoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schede/{schedaId}/giorni")
@CrossOrigin(origins = "*")
public class GiornoController {

    private final GiornoService service;

    public GiornoController(GiornoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Giorno> perScheda(@PathVariable Long schedaId) {
        return service.trovaPerScheda(schedaId);
    }

    @PostMapping
    public Giorno crea(@PathVariable Long schedaId, @RequestBody Giorno giorno) {
        return service.salva(schedaId, giorno);
    }

    @PutMapping("/{id}")
    public Giorno modifica(@PathVariable Long schedaId, @PathVariable Long id, @RequestBody Giorno giorno) {
        giorno.setId(id);
        return service.salva(schedaId, giorno);
    }

    @DeleteMapping("/{id}")
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }
}