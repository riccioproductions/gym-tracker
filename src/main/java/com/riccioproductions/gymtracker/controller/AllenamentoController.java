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
        return service.registraSerie(id,
                toLong(body.get("esercizioGiornoId")),
                toInt(body.get("numeroSerie")),
                toInt(body.get("ripetizioniEffettuate")),
                toDouble(body.get("caricoEffettivo")), // il carico può essere null
                toStr(body.get("lato"))); // "SX"/"DX" per gli unilaterali, altrimenti null
    }

    //Corregge ripetizioni/carico di una serie già registrata.
    @PutMapping("/{id}/serie/{serieId}")
    public SerieEseguita modificaSerie(@PathVariable Long id, @PathVariable Long serieId,
                                       @RequestBody Map<String, Object> body) {
        return service.modificaSerie(id, serieId,
                toInt(body.get("ripetizioniEffettuate")),
                toDouble(body.get("caricoEffettivo")));
    }

    //Toglie la spunta a una serie.
    @DeleteMapping("/{id}/serie/{serieId}")
    public void eliminaSerie(@PathVariable Long id, @PathVariable Long serieId) {
        service.eliminaSerie(id, serieId);
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

    @GetMapping("/recenti")
    public List<Allenamento> recenti(@RequestParam(defaultValue = "8") int limite) {
        return service.recenti(limite);
    }

    private static boolean vuoto(Object o) {
        return o == null || o.toString().isBlank();
    }
    private static Long toLong(Object o) { return vuoto(o) ? null : Long.valueOf(o.toString()); }
    private static Integer toInt(Object o) { return vuoto(o) ? null : Integer.valueOf(o.toString()); }
    private static Double toDouble(Object o) { return vuoto(o) ? null : Double.valueOf(o.toString()); }
    private static String toStr(Object o) { return vuoto(o) ? null : o.toString().trim(); }
}
