package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.*;
import com.riccioproductions.gymtracker.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AllenamentoService {

    private final AllenamentoRepository allenamentoRepository;
    private final GiornoRepository giornoRepository;
    private final SerieEseguitaRepository serieRepository;
    private final EsercizioGiornoRepository esercizioGiornoRepository;
    private final UtenteRepository utenteRepository;

    public AllenamentoService(AllenamentoRepository allenamentoRepository,
                              GiornoRepository giornoRepository,
                              SerieEseguitaRepository serieRepository,
                              EsercizioGiornoRepository esercizioGiornoRepository,
                              UtenteRepository utenteRepository) {
        this.allenamentoRepository = allenamentoRepository;
        this.giornoRepository = giornoRepository;
        this.serieRepository = serieRepository;
        this.esercizioGiornoRepository = esercizioGiornoRepository;
        this.utenteRepository = utenteRepository;
    }

    public Optional<Allenamento> trovaInCorso(Long utenteId) {
        return allenamentoRepository.findFirstByUtenteIdAndStatoOrderByDataInizioDesc(utenteId, StatoAllenamento.IN_CORSO);
    }

    public Allenamento avvia(Long utenteId, Long giornoId) {
        trovaInCorso(utenteId).ifPresent(a -> {
            throw new RuntimeException("C'è già un allenamento in corso (id=" + a.getId() + ")");
        });

        Giorno giorno = giornoRepository.findByIdAndSchedaUtenteId(giornoId, utenteId)
                .orElseThrow(() -> new RuntimeException("Giorno non trovato: " + giornoId));

        Allenamento allenamento = new Allenamento();
        allenamento.setUtente(utenteRepository.getReferenceById(utenteId));
        allenamento.setGiorno(giorno);
        allenamento.setDataInizio(LocalDateTime.now());
        allenamento.setStato(StatoAllenamento.IN_CORSO);
        return allenamentoRepository.save(allenamento);
    }

    public SerieEseguita registraSerie(Long utenteId, Long allenamentoId, Long esercizioGiornoId,
                                       Integer numeroSerie, Integer ripetizioniEffettuate,
                                       Double caricoEffettivo, String lato) {
        if (esercizioGiornoId == null || numeroSerie == null || ripetizioniEffettuate == null) {
            throw new RuntimeException("Dati della serie incompleti");
        }
        Allenamento allenamento = trovaPerId(allenamentoId, utenteId);

        EsercizioGiorno eg = esercizioGiornoRepository.findByIdAndGiornoSchedaUtenteId(esercizioGiornoId, utenteId)
                .orElseThrow(() -> new RuntimeException("EsercizioGiorno non trovato: " + esercizioGiornoId));
        if (!eg.getGiorno().getId().equals(allenamento.getGiorno().getId())) {
            throw new RuntimeException("L'esercizio non appartiene al giorno di questo allenamento");
        }

        String latoNorm = (lato == null || lato.isBlank()) ? null : lato.trim().toUpperCase();
        if (latoNorm != null && !latoNorm.equals("SX") && !latoNorm.equals("DX")) {
            throw new RuntimeException("Lato non valido: " + lato);
        }

        //Evita doppioni (es. doppio tocco sul pulsante "Fatta"); il lato distingue SX e DX della stessa serie
        boolean giaPresente = serieRepository.findByAllenamentoId(allenamentoId).stream()
                .anyMatch(s -> s.getEsercizioGiorno().getId().equals(esercizioGiornoId)
                        && s.getNumeroSerie().equals(numeroSerie)
                        && Objects.equals(s.getLato(), latoNorm));
        if (giaPresente) {
            throw new RuntimeException("Serie " + numeroSerie + (latoNorm != null ? " " + latoNorm : "") + " già registrata");
        }

        SerieEseguita serie = new SerieEseguita();
        serie.setAllenamento(allenamento);
        serie.setEsercizioGiorno(eg);
        serie.setNomeEsercizio(eg.getEsercizio().getNome());
        serie.setNumeroSerie(numeroSerie);
        serie.setLato(latoNorm);
        serie.setRipetizioniEffettuate(ripetizioniEffettuate);
        serie.setCaricoEffettivo(caricoEffettivo);
        serie.setTimestampCompletamento(LocalDateTime.now());

        return serieRepository.save(serie);
    }

    @Transactional
    public void eliminaSerie(Long utenteId, Long allenamentoId, Long serieId) {
        serieRepository.delete(trovaSerie(utenteId, allenamentoId, serieId));
    }

    @Transactional
    public SerieEseguita modificaSerie(Long utenteId, Long allenamentoId, Long serieId,
                                       Integer ripetizioni, Double carico) {
        SerieEseguita s = trovaSerie(utenteId, allenamentoId, serieId);
        if (ripetizioni != null) s.setRipetizioniEffettuate(ripetizioni);
        s.setCaricoEffettivo(carico);
        return serieRepository.save(s);
    }

    private SerieEseguita trovaSerie(Long utenteId, Long allenamentoId, Long serieId) {
        trovaPerId(allenamentoId, utenteId); // l'allenamento deve essere dell'utente
        SerieEseguita s = serieRepository.findById(serieId)
                .orElseThrow(() -> new RuntimeException("Serie non trovata: " + serieId));
        if (!s.getAllenamento().getId().equals(allenamentoId)) {
            throw new RuntimeException("La serie non appartiene a questo allenamento");
        }
        return s;
    }

    public Allenamento termina(Long utenteId, Long allenamentoId) {
        Allenamento allenamento = trovaPerId(allenamentoId, utenteId);
        allenamento.setDataFine(LocalDateTime.now());
        allenamento.setStato(StatoAllenamento.COMPLETATO);
        return allenamentoRepository.save(allenamento);
    }

    public Allenamento trovaPerId(Long id, Long utenteId) {
        return allenamentoRepository.findByIdAndUtenteId(id, utenteId)
                .orElseThrow(() -> new RuntimeException("Allenamento non trovato: " + id));
    }

    public List<SerieEseguita> serieDiAllenamento(Long utenteId, Long allenamentoId) {
        trovaPerId(allenamentoId, utenteId);
        return serieRepository.findByAllenamentoId(allenamentoId);
    }

    public List<Allenamento> storicoPerGiorno(Long utenteId, Long giornoId) {
        return allenamentoRepository.findByUtenteIdAndGiornoIdOrderByDataInizioDesc(utenteId, giornoId);
    }

    public void elimina(Long utenteId, Long id) {
        trovaPerId(id, utenteId);
        allenamentoRepository.deleteById(id);
    }

    public List<Allenamento> recenti(Long utenteId, int limite) {
        return allenamentoRepository.findByUtenteIdOrderByDataInizioDesc(utenteId, PageRequest.of(0, limite));
    }
}
