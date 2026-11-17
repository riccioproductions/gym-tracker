package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.*;
import com.riccioproductions.gymtracker.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AllenamentoService {

    private final AllenamentoRepository allenamentoRepository;
    private final GiornoRepository giornoRepository;
    private final SerieEseguitaRepository serieRepository;
    private final EsercizioGiornoRepository esercizioGiornoRepository;

    public AllenamentoService(AllenamentoRepository allenamentoRepository,
                              GiornoRepository giornoRepository,
                              SerieEseguitaRepository serieRepository,
                              EsercizioGiornoRepository esercizioGiornoRepository) {
        this.allenamentoRepository = allenamentoRepository;
        this.giornoRepository = giornoRepository;
        this.serieRepository = serieRepository;
        this.esercizioGiornoRepository = esercizioGiornoRepository;
    }

    public Optional<Allenamento> trovaInCorso() {
        return allenamentoRepository.findFirstByStatoOrderByDataInizioDesc(StatoAllenamento.IN_CORSO);
    }

    public Allenamento avvia(Long giornoId) {
        trovaInCorso().ifPresent(a -> {
            throw new RuntimeException("C'è già un allenamento in corso (id=" + a.getId() + ")");
        });

        Giorno giorno = giornoRepository.findById(giornoId)
                .orElseThrow(() -> new RuntimeException("Giorno non trovato: " + giornoId));

        Allenamento allenamento = new Allenamento();
        allenamento.setGiorno(giorno);
        allenamento.setDataInizio(LocalDateTime.now());
        allenamento.setStato(StatoAllenamento.IN_CORSO);
        return allenamentoRepository.save(allenamento);
    }

    public SerieEseguita registraSerie(Long allenamentoId, Long esercizioGiornoId,
                                       Integer numeroSerie, Integer ripetizioniEffettuate,
                                       Double caricoEffettivo) {
        if (esercizioGiornoId == null || numeroSerie == null || ripetizioniEffettuate == null) {
            throw new RuntimeException("Dati della serie incompleti");
        }
        Allenamento allenamento = allenamentoRepository.findById(allenamentoId)
                .orElseThrow(() -> new RuntimeException("Allenamento non trovato: " + allenamentoId));

        EsercizioGiorno eg = esercizioGiornoRepository.findById(esercizioGiornoId)
                .orElseThrow(() -> new RuntimeException("EsercizioGiorno non trovato: " + esercizioGiornoId));

        //Evita doppioni (es. doppio tocco sul pulsante "Fatta")
        boolean giaPresente = serieRepository.findByAllenamentoId(allenamentoId).stream()
                .anyMatch(s -> s.getEsercizioGiorno().getId().equals(esercizioGiornoId)
                        && s.getNumeroSerie().equals(numeroSerie));
        if (giaPresente) {
            throw new RuntimeException("Serie " + numeroSerie + " già registrata");
        }

        SerieEseguita serie = new SerieEseguita();
        serie.setAllenamento(allenamento);
        serie.setEsercizioGiorno(eg);
        serie.setNomeEsercizio(eg.getEsercizio().getNome());
        serie.setNumeroSerie(numeroSerie);
        serie.setRipetizioniEffettuate(ripetizioniEffettuate);
        serie.setCaricoEffettivo(caricoEffettivo);
        serie.setTimestampCompletamento(LocalDateTime.now());

        return serieRepository.save(serie);
    }

    @Transactional
    public void eliminaSerie(Long allenamentoId, Long serieId) {
        serieRepository.delete(trovaSerie(allenamentoId, serieId));
    }

    @Transactional
    public SerieEseguita modificaSerie(Long allenamentoId, Long serieId,
                                       Integer ripetizioni, Double carico) {
        SerieEseguita s = trovaSerie(allenamentoId, serieId);
        if (ripetizioni != null) s.setRipetizioniEffettuate(ripetizioni);
        s.setCaricoEffettivo(carico);
        return serieRepository.save(s);
    }

    private SerieEseguita trovaSerie(Long allenamentoId, Long serieId) {
        SerieEseguita s = serieRepository.findById(serieId)
                .orElseThrow(() -> new RuntimeException("Serie non trovata: " + serieId));
        if (!s.getAllenamento().getId().equals(allenamentoId)) {
            throw new RuntimeException("La serie non appartiene a questo allenamento");
        }
        return s;
    }

    public Allenamento termina(Long allenamentoId) {
        Allenamento allenamento = allenamentoRepository.findById(allenamentoId)
                .orElseThrow(() -> new RuntimeException("Allenamento non trovato: " + allenamentoId));
        allenamento.setDataFine(LocalDateTime.now());
        allenamento.setStato(StatoAllenamento.COMPLETATO);
        return allenamentoRepository.save(allenamento);
    }

    public Allenamento trovaPerId(Long id) {
        return allenamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allenamento non trovato: " + id));
    }

    public List<SerieEseguita> serieDiAllenamento(Long allenamentoId) {
        return serieRepository.findByAllenamentoId(allenamentoId);
    }

    public List<Allenamento> storicoPerGiorno(Long giornoId) {
        return allenamentoRepository.findByGiornoIdOrderByDataInizioDesc(giornoId);
    }

    public void elimina(Long id) {
        allenamentoRepository.deleteById(id);
    }

    public List<Allenamento> recenti(int limite) {
        return allenamentoRepository.findAllByOrderByDataInizioDesc(PageRequest.of(0, limite));
    }
}
