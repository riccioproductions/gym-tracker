package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.dto.AllenamentoSyncDTO;
import com.riccioproductions.gymtracker.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*") 
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/allenamento")
    public ResponseEntity<String> syncAllenamento(@RequestBody AllenamentoSyncDTO dto) {
        try {
            String risultato = syncService.syncAllenamento(dto);
            return ResponseEntity.ok(risultato); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante la sync: " + e.getMessage()); 
        }
    }
}