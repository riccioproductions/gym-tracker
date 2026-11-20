package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.model.Allenamento;
import com.riccioproductions.gymtracker.model.SerieEseguita;
import com.riccioproductions.gymtracker.model.Utente;
import com.riccioproductions.gymtracker.service.AllenamentoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/allenamenti")
public class AllenamentoController {

    private final AllenamentoService service;

    public AllenamentoController(AllenamentoService service) {
        this.service = service;
    }

    @GetMapping("/in-corso")
    public Optional<Allenamento> inCorso(@AuthenticationPrincipal Utente utente) {
        return service.trovaInCorso(utente.getId());
    }

    @PostMapping("/avvia/{giornoId}")
    public Allenamento avvia(@AuthenticationPrincipal Utente utente, @PathVariable Long giornoId) {
        return service.avvia(utente.getId(), giornoId);
    }

    @GetMapping("/{id}")
    public Allenamento dettaglio(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        return service.trovaPerId(id, utente.getId());
    }

    @GetMapping("/{id}/serie")
    public List<SerieEseguita> serie(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        return service.serieDiAllenamento(utente.getId(), id);
    }

    @PostMapping("/{id}/serie")
    public SerieEseguita registraSerie(@AuthenticationPrincipal Utente utente, @PathVariable Long id,
                                       @RequestBody Map<String, Object> body) {
        return service.registraSerie(utente.getId(), id,
                toLong(body.get("esercizioGiornoId")),
                toInt(body.get("numeroSerie")),
                toInt(body.get("ripetizioniEffettuate")),
                toDouble(body.get("caricoEffettivo")), // il carico può essere null
                toStr(body.get("lato"))); // "SX"/"DX" per gli unilaterali, altrimenti null
    }

    //Corregge ripetizioni/carico di una serie già registrata.
    @PutMapping("/{id}/serie/{serieId}")
    public SerieEseguita modificaSerie(@AuthenticationPrincipal Utente utente, @PathVariable Long id,
                                       @PathVariable Long serieId, @RequestBody Map<String, Object> body) {
        return service.modificaSerie(utente.getId(), id, serieId,
                toInt(body.get("ripetizioniEffettuate")),
                toDouble(body.get("caricoEffettivo")));
    }

    //Toglie la spunta a una serie.
    @DeleteMapping("/{id}/serie/{serieId}")
    public void eliminaSerie(@AuthenticationPrincipal Utente utente, @PathVariable Long id, @PathVariable Long serieId) {
        service.eliminaSerie(utente.getId(), id, serieId);
    }

    @PutMapping("/{id}/termina")
    public Allenamento termina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        return service.termina(utente.getId(), id);
    }

    @GetMapping("/storico/giorno/{giornoId}")
    public List<Allenamento> storico(@AuthenticationPrincipal Utente utente, @PathVariable Long giornoId) {
        return service.storicoPerGiorno(utente.getId(), giornoId);
    }

    @DeleteMapping("/{id}")
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        service.elimina(utente.getId(), id);
    }

    @GetMapping("/recenti")
    public List<Allenamento> recenti(@AuthenticationPrincipal Utente utente,
                                     @RequestParam(defaultValue = "8") int limite) {
        return service.recenti(utente.getId(), limite);
    }

    private static boolean vuoto(Object o) {
        return o == null || o.toString().isBlank();
    }
    private static Long toLong(Object o) { return vuoto(o) ? null : Long.valueOf(o.toString()); }
    private static Integer toInt(Object o) { return vuoto(o) ? null : Integer.valueOf(o.toString()); }
    private static Double toDouble(Object o) { return vuoto(o) ? null : Double.valueOf(o.toString()); }
    private static String toStr(Object o) { return vuoto(o) ? null : o.toString().trim(); }
}
