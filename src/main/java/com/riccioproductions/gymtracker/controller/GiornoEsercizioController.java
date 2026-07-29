package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.entity.GiornoEsercizio;
import com.riccioproductions.gymtracker.repository.GiornoEsercizioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giorni")
@CrossOrigin(origins = "*")
public class GiornoEsercizioController {

    private final GiornoEsercizioRepository giornoEsercizioRepository;

    public GiornoEsercizioController(GiornoEsercizioRepository giornoEsercizioRepository) {
        this.giornoEsercizioRepository = giornoEsercizioRepository;
    }

    @GetMapping("/{idGiorno}/esercizi")
    public ResponseEntity<List<GiornoEsercizio>> getEserciziDelGiorno(@PathVariable Long idGiorno) {
        return ResponseEntity.ok(giornoEsercizioRepository.findByGiorno_IdGiornoOrderByOrdineAsc(idGiorno));
    }
}