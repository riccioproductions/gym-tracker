package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Allenamento;
import com.riccioproductions.gymtracker.model.SerieEseguita;
import com.riccioproductions.gymtracker.service.AllenamentoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/allenamenti")
@CrossOrigin(origins = "*")
public class AllenamentoController {

    private final AllenamentoService service;

    public AllenamentoController(AllenamentoService service) {
        this.service = service;
    }

    @GetMapping("/in-corso")
    public Optional<Allenamento> inCorso() {
        return service.trovaInCorso();
    }

    @PostMapping("/avvia/{giornoId}")
    public Allenamento avvia(@PathVariable Long giornoId) {
        return service.avvia(giornoId);
    }

    @GetMapping("/{id}")
    public Allenamento dettaglio(@PathVariable Long id) {
        return service.trovaPerId(id);
    }

    @GetMapping("/{id}/serie")
    public List<SerieEseguita> serie(@PathVariable Long id) {
        return service.serieDiAllenamento(id);
    }

    @PostMapping("/{id}/serie")
    public SerieEseguita registraSerie(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long esercizioGiornoId = Long.valueOf(body.get("esercizioGiornoId").toString());
        Integer numeroSerie = Integer.valueOf(body.get("numeroSerie").toString());
        Integer ripetizioniEffettuate = Integer.valueOf(body.get("ripetizioniEffettuate").toString());
        Double caricoEffettivo = Double.valueOf(body.get("caricoEffettivo").toString());
        return service.registraSerie(id, esercizioGiornoId, numeroSerie, ripetizioniEffettuate, caricoEffettivo);
    }

    @PutMapping("/{id}/termina")
    public Allenamento termina(@PathVariable Long id) {
        return service.termina(id);
    }

    @GetMapping("/storico/giorno/{giornoId}")
    public List<Allenamento> storico(@PathVariable Long giornoId) {
        return service.storicoPerGiorno(giornoId);
    }
	
	@DeleteMapping("/{id}")
	public void elimina(@PathVariable Long id) {
		service.elimina(id);
	}
}