package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchedaService {

    private final SchedaRepository repository;

    public SchedaService(SchedaRepository repository) {
        this.repository = repository;
    }

    public List<Scheda> trovaTutte() {
        return repository.findAll();
    }

    public Scheda trovaPerId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheda non trovata: " + id));
    }

    public Scheda salva(Scheda scheda) {
        return repository.save(scheda);
    }

    public void elimina(Long id) {
        repository.deleteById(id);
    }
}