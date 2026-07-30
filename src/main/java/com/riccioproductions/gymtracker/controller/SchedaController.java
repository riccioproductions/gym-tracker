package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.service.SchedaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schede")
@CrossOrigin(origins = "*")
public class SchedaController {

    private final SchedaService service;

    public SchedaController(SchedaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Scheda> tutte() {
        return service.trovaTutte();
    }

    @GetMapping("/{id}")
    public Scheda unaSingola(@PathVariable Long id) {
        return service.trovaPerId(id);
    }

    @PostMapping
    public Scheda crea(@RequestBody Scheda scheda) {
        return service.salva(scheda);
    }

    @PutMapping("/{id}")
    public Scheda modifica(@PathVariable Long id, @RequestBody Scheda scheda) {
        scheda.setId(id);
        return service.salva(scheda);
    }

    @DeleteMapping("/{id}")
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }
}