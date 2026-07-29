package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.dto.SerieStoricoDTO;
import com.riccioproductions.gymtracker.service.StatisticheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*") 
public class StatisticheController {

    private final StatisticheService statisticheService;

    public StatisticheController(StatisticheService statisticheService) {
        this.statisticheService = statisticheService;
    }

    @GetMapping("/storico")
    public ResponseEntity<List<SerieStoricoDTO>> getStoricoEsercizio(
            @RequestParam Long idUtente, 
            @RequestParam Long idEsercizio) {
        
        try {
            List<SerieStoricoDTO> storico = statisticheService.getStoricoEsercizio(idUtente, idEsercizio);
            return ResponseEntity.ok(storico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}