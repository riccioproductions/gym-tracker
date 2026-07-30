package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.repository.EsercizioGiornoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.GiornoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EsercizioGiornoService {

    private final EsercizioGiornoRepository repository;
    private final GiornoRepository giornoRepository;
    private final EsercizioRepository esercizioRepository;

    public EsercizioGiornoService(EsercizioGiornoRepository repository,
                                   GiornoRepository giornoRepository,
                                   EsercizioRepository esercizioRepository) {
        this.repository = repository;
        this.giornoRepository = giornoRepository;
        this.esercizioRepository = esercizioRepository;
    }

    public List<EsercizioGiorno> trovaPerGiorno(Long giornoId) {
        return repository.findByGiornoIdOrderByOrdineAsc(giornoId);
    }

    public EsercizioGiorno salva(Long giornoId, Long esercizioId, EsercizioGiorno eg) {
        Giorno giorno = giornoRepository.findById(giornoId)
                .orElseThrow(() -> new RuntimeException("Giorno non trovato: " + giornoId));
        Esercizio esercizio = esercizioRepository.findById(esercizioId)
                .orElseThrow(() -> new RuntimeException("Esercizio non trovato: " + esercizioId));
        eg.setGiorno(giorno);
        eg.setEsercizio(esercizio);
        return repository.save(eg);
    }

    public void elimina(Long id) {
        repository.deleteById(id);
    }
}