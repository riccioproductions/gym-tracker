package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchedaService {

    private final SchedaRepository repository;
    private final UtenteRepository utenteRepository;

    public SchedaService(SchedaRepository repository, UtenteRepository utenteRepository) {
        this.repository = repository;
        this.utenteRepository = utenteRepository;
    }

    public List<Scheda> trovaTutte(Long utenteId) {
        return repository.findByUtenteIdOrderByIdAsc(utenteId);
    }

    // Una scheda di un altro utente risponde come se non esistesse
    public Scheda trovaPerId(Long id, Long utenteId) {
        return repository.findByIdAndUtenteId(id, utenteId)
                .orElseThrow(() -> new RuntimeException("Scheda non trovata: " + id));
    }

    public Scheda salva(Long utenteId, Scheda scheda) {
        if (scheda.getId() != null) {
            // Aggiornamento: modifichiamo SOLO i campi voluti sull'entità esistente,
            // senza toccare la collezione "giorni" già presente nel database
            Scheda esistente = trovaPerId(scheda.getId(), utenteId);
            esistente.setNome(scheda.getNome());
            esistente.setDescrizione(scheda.getDescrizione());
            return repository.save(esistente);
        }
        scheda.setUtente(utenteRepository.getReferenceById(utenteId));
        scheda.setGiorni(new ArrayList<>());
        return repository.save(scheda);
    }

    public void elimina(Long id, Long utenteId) {
        trovaPerId(id, utenteId);
        repository.deleteById(id);
    }
}
