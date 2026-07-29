package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.entity.Esercizio;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/esercizi")
@CrossOrigin(origins = "*")
public class EsercizioController {

    private final EsercizioRepository esercizioRepository;

    public EsercizioController(EsercizioRepository esercizioRepository) {
        this.esercizioRepository = esercizioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Esercizio>> getTuttiEsercizi() {
        return ResponseEntity.ok(esercizioRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Esercizio> creaEsercizio(@RequestBody Esercizio esercizio) {
        // Per ora salviamo l'esercizio come "globale". 
        // In futuro si potrebbe associare al JWT dell'utente loggato.
        Esercizio salvato = esercizioRepository.save(esercizio);
        return ResponseEntity.ok(salvato);
    }
}