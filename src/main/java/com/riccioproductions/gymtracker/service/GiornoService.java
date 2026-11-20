package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.repository.GiornoRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class GiornoService {

    private final GiornoRepository repository;
    private final SchedaService schedaService;

    public GiornoService(GiornoRepository repository, SchedaService schedaService) {
        this.repository = repository;
        this.schedaService = schedaService;
    }

    public List<Giorno> trovaPerScheda(Long schedaId, Long utenteId) {
        schedaService.trovaPerId(schedaId, utenteId); // verifica che la scheda sia dell'utente
        return repository.findBySchedaIdOrderByOrdineAsc(schedaId);
    }

    public Giorno trovaPerId(Long id, Long utenteId) {
        return repository.findByIdAndSchedaUtenteId(id, utenteId)
                .orElseThrow(() -> new RuntimeException("Giorno non trovato: " + id));
    }

    public Giorno salva(Long utenteId, Long schedaId, Giorno giorno) {
        Scheda scheda = schedaService.trovaPerId(schedaId, utenteId);

        if (giorno.getId() != null) {
            Giorno esistente = trovaPerId(giorno.getId(), utenteId);
            esistente.setNome(giorno.getNome());
            esistente.setOrdine(giorno.getOrdine());
            esistente.setScheda(scheda);
            return repository.save(esistente);
        }

        giorno.setScheda(scheda);
        giorno.setEsercizi(new ArrayList<>());
        return repository.save(giorno);
    }

    public void elimina(Long id, Long utenteId) {
        trovaPerId(id, utenteId);
        repository.deleteById(id);
    }
}
