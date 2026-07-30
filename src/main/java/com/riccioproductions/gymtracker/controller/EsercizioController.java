package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.service.EsercizioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/esercizi")
@CrossOrigin(origins = "*")
public class EsercizioController {

    private final EsercizioService service;

    public EsercizioController(EsercizioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Esercizio> tutti() {
        return service.trovaTutti();
    }

    @PostMapping
    public Esercizio crea(@RequestBody Esercizio esercizio) {
        return service.salva(esercizio);
    }

    @PutMapping("/{id}")
    public Esercizio modifica(@PathVariable Long id, @RequestBody Esercizio esercizio) {
        esercizio.setId(id);
        return service.salva(esercizio);
    }

    @DeleteMapping("/{id}")
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }
}