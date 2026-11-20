package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import com.riccioproductions.gymtracker.model.Giorno;
import com.riccioproductions.gymtracker.repository.EsercizioGiornoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EsercizioGiornoService {

    private final EsercizioGiornoRepository repository;
    private final GiornoService giornoService;
    private final EsercizioService esercizioService;

    public EsercizioGiornoService(EsercizioGiornoRepository repository,
                                  GiornoService giornoService,
                                  EsercizioService esercizioService) {
        this.repository = repository;
        this.giornoService = giornoService;
        this.esercizioService = esercizioService;
    }

    public List<EsercizioGiorno> trovaPerGiorno(Long giornoId, Long utenteId) {
        giornoService.trovaPerId(giornoId, utenteId); // verifica che il giorno sia dell'utente
        return repository.findByGiornoIdOrderByOrdineAsc(giornoId);
    }

    public EsercizioGiorno salva(Long utenteId, Long giornoId, Long esercizioId, EsercizioGiorno eg) {
        Giorno giorno = giornoService.trovaPerId(giornoId, utenteId);
        Esercizio esercizio = esercizioService.trovaPerId(esercizioId, utenteId);
        if (eg.getId() != null) {
            // Modifica: la riga deve essere dell'utente (altrimenti si sovrascriverebbe quella di un altro)
            repository.findByIdAndGiornoSchedaUtenteId(eg.getId(), utenteId)
                    .orElseThrow(() -> new RuntimeException("Esercizio del giorno non trovato: " + eg.getId()));
        }
        eg.setGiorno(giorno);
        eg.setEsercizio(esercizio);
        return repository.save(eg);
    }

    public void elimina(Long id, Long utenteId) {
        repository.findByIdAndGiornoSchedaUtenteId(id, utenteId)
                .orElseThrow(() -> new RuntimeException("Esercizio del giorno non trovato: " + id));
        repository.deleteById(id);
    }
}
