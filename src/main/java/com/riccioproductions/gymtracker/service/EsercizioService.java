package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EsercizioService {

    private final EsercizioRepository repository;

    public EsercizioService(EsercizioRepository repository) {
        this.repository = repository;
    }

    public List<Esercizio> trovaTutti() {
        return repository.findAll();
    }

    public Esercizio salva(Esercizio esercizio) {
        return repository.save(esercizio);
    }

    public void elimina(Long id) {
        repository.deleteById(id);
    }
}