package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EsercizioService {

    private final EsercizioRepository repository;
    private final UtenteRepository utenteRepository;

    public EsercizioService(EsercizioRepository repository, UtenteRepository utenteRepository) {
        this.repository = repository;
        this.utenteRepository = utenteRepository;
    }

    public List<Esercizio> trovaTutti(Long utenteId) {
        return repository.findByUtenteId(utenteId);
    }

    public Esercizio trovaPerId(Long id, Long utenteId) {
        return repository.findByIdAndUtenteId(id, utenteId)
                .orElseThrow(() -> new RuntimeException("Esercizio non trovato: " + id));
    }

    public Esercizio salva(Long utenteId, Esercizio esercizio) {
        if (esercizio.getId() != null) {
            Esercizio esistente = trovaPerId(esercizio.getId(), utenteId);
            esistente.setNome(esercizio.getNome());
            esistente.setGruppoMuscolare(esercizio.getGruppoMuscolare());
            return repository.save(esistente);
        }
        esercizio.setUtente(utenteRepository.getReferenceById(utenteId));
        return repository.save(esercizio);
    }

    public void elimina(Long id, Long utenteId) {
        trovaPerId(id, utenteId);
        repository.deleteById(id);
    }
}
