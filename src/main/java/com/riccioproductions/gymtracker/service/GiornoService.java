package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.repository.GiornoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GiornoService {

    private final GiornoRepository repository;
    private final SchedaService schedaService;

    public GiornoService(GiornoRepository repository, SchedaService schedaService) {
        this.repository = repository;
        this.schedaService = schedaService;
    }

    public List<Giorno> trovaPerScheda(Long schedaId) {
        return repository.findBySchedaIdOrderByOrdineAsc(schedaId);
    }

    public Giorno salva(Long schedaId, Giorno giorno) {
        Scheda scheda = schedaService.trovaPerId(schedaId);
        giorno.setScheda(scheda);
        return repository.save(giorno);
    }

    public void elimina(Long id) {
        repository.deleteById(id);
    }
}