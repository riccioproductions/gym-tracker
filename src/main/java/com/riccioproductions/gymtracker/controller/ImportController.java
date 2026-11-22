package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.ImportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

// Importa nell'account collegato un file prodotto da /api/export.
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, Integer> importa(@AuthenticationPrincipal Utente utente, @RequestBody Map<String, Object> corpo) {
        return service.importa(utente.getId(), corpo);
    }
}
