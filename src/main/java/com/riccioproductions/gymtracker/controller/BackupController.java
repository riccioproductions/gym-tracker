package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.repository.AllenamentoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Esporta tutti i dati in un unico JSON (solo lettura: nessun ripristino automatico).
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class BackupController {

    private final SchedaRepository schedaRepository;
    private final EsercizioRepository esercizioRepository;
    private final AllenamentoRepository allenamentoRepository;

    public BackupController(SchedaRepository schedaRepository,
                            EsercizioRepository esercizioRepository,
                            AllenamentoRepository allenamentoRepository) {
        this.schedaRepository = schedaRepository;
        this.esercizioRepository = esercizioRepository;
        this.allenamentoRepository = allenamentoRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> esporta() {
        Map<String, Object> dati = new LinkedHashMap<>();
        dati.put("versione", 1);
        dati.put("esportatoIl", LocalDateTime.now().toString());
        dati.put("esercizi", esercizioRepository.findAll());
        dati.put("schede", schedaRepository.findAll());
        dati.put("allenamenti", allenamentoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataInizio")));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gymtracker-backup.json\"")
                .body(dati);
    }
}
