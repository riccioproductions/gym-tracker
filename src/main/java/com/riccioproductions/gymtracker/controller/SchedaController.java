package com.riccioproductions.gymtracker.controller;

import com.riccioproductions.gymtracker.entity.Esercizio;
import com.riccioproductions.gymtracker.entity.GiornoEsercizio;
import com.riccioproductions.gymtracker.entity.Scheda;
import com.riccioproductions.gymtracker.entity.SchedaGiorno;
import com.riccioproductions.gymtracker.entity.Utente;
import com.riccioproductions.gymtracker.repository.GiornoEsercizioRepository;
import com.riccioproductions.gymtracker.repository.SchedaGiornoRepository;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/schede")
@CrossOrigin(origins = "*")
public class SchedaController {

    private final SchedaRepository schedaRepository;
    private final SchedaGiornoRepository schedaGiornoRepository;
    private final GiornoEsercizioRepository giornoEsercizioRepository;

    public SchedaController(SchedaRepository schedaRepository, 
                            SchedaGiornoRepository schedaGiornoRepository,
                            GiornoEsercizioRepository giornoEsercizioRepository) {
        this.schedaRepository = schedaRepository;
        this.schedaGiornoRepository = schedaGiornoRepository;
        this.giornoEsercizioRepository = giornoEsercizioRepository;
    }

    @GetMapping("/utente/{idUtente}")
    public ResponseEntity<List<Scheda>> getSchedeUtente(@PathVariable Long idUtente) {
        return ResponseEntity.ok(schedaRepository.findByUtente_IdUtente(idUtente));
    }

    @GetMapping("/{idScheda}/giorni")
    public ResponseEntity<List<SchedaGiorno>> getGiorniScheda(@PathVariable Long idScheda) {
        return ResponseEntity.ok(schedaGiornoRepository.findByScheda_IdSchedaOrderByOrdineAsc(idScheda));
    }

    @PostMapping("/crea")
    @Transactional
    public ResponseEntity<String> creaScheda(@RequestBody com.riccioproductions.gymtracker.dto.SchedaDTO dto) {
        Utente utente = new Utente();
        utente.setIdUtente(1L); // Mock utente 1

        Scheda scheda = new Scheda();
        scheda.setNome(dto.getNome());
        scheda.setDescrizione(dto.getDescrizione());
        scheda.setUtente(utente);
        
        Scheda savedScheda = schedaRepository.save(scheda);

        int ordineGiorno = 1;
        for (com.riccioproductions.gymtracker.dto.SchedaDTO.GiornoDTO giornoDto : dto.getGiorni()) {
            SchedaGiorno giorno = new SchedaGiorno();
            giorno.setNomeGiorno(giornoDto.getNomeGiorno());
            giorno.setOrdine(ordineGiorno++);
            giorno.setScheda(savedScheda);
            SchedaGiorno savedGiorno = schedaGiornoRepository.save(giorno);

            int ordineEsercizio = 1;
            for (com.riccioproductions.gymtracker.dto.SchedaDTO.EsercizioGiornoDTO esDto : giornoDto.getEsercizi()) {
                 GiornoEsercizio ge = new GiornoEsercizio();
                 ge.setGiorno(savedGiorno);
                 
                 Esercizio esercizio = new Esercizio();
                 esercizio.setIdEsercizio(esDto.getIdEsercizio());
                 ge.setEsercizio(esercizio);
                 
                 ge.setOrdine(ordineEsercizio++);
                 ge.setSerieTarget(esDto.getSerieTarget());
                 ge.setRipetizioniTarget(esDto.getRipetizioniTarget());
                 ge.setRecuperoSecondi(esDto.getRecuperoSecondi());
                 ge.setNote(esDto.getNote());
                 
                 giornoEsercizioRepository.save(ge);
            }
        }
        return ResponseEntity.ok("Scheda salvata con successo");
    }
    
    @DeleteMapping("/{idScheda}")
    @Transactional
    public ResponseEntity<Void> eliminaScheda(@PathVariable Long idScheda) {
        schedaRepository.deleteById(idScheda);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idScheda}")
    @Transactional
    public ResponseEntity<String> aggiornaScheda(@PathVariable Long idScheda, @RequestBody com.riccioproductions.gymtracker.dto.SchedaDTO dto) {
        Scheda scheda = schedaRepository.findById(idScheda).orElseThrow();
        schedaGiornoRepository.deleteAll(schedaGiornoRepository.findByScheda_IdSchedaOrderByOrdineAsc(idScheda));
        return ResponseEntity.ok("Scheda aggiornata");
    }
}