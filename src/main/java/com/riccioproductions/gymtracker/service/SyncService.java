package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.dto.AllenamentoSyncDTO;
import com.riccioproductions.gymtracker.dto.SerieSyncDTO;
import com.riccioproductions.gymtracker.entity.Allenamento;
import com.riccioproductions.gymtracker.entity.Esercizio;
import com.riccioproductions.gymtracker.entity.SchedaGiorno;
import com.riccioproductions.gymtracker.entity.SerieLog;
import com.riccioproductions.gymtracker.entity.Utente;
import com.riccioproductions.gymtracker.repository.AllenamentoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.SchedaGiornoRepository;
import com.riccioproductions.gymtracker.repository.SerieLogRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SyncService {

    private final AllenamentoRepository allenamentoRepository;
    private final SerieLogRepository serieLogRepository;
    private final UtenteRepository utenteRepository;
    private final EsercizioRepository esercizioRepository;
    private final SchedaGiornoRepository schedaGiornoRepository;

    public SyncService(AllenamentoRepository allenamentoRepository, 
                       SerieLogRepository serieLogRepository,
                       UtenteRepository utenteRepository,
                       EsercizioRepository esercizioRepository,
                       SchedaGiornoRepository schedaGiornoRepository) {
        this.allenamentoRepository = allenamentoRepository;
        this.serieLogRepository = serieLogRepository;
        this.utenteRepository = utenteRepository;
        this.esercizioRepository = esercizioRepository;
        this.schedaGiornoRepository = schedaGiornoRepository;
    }

    @Transactional
    public String syncAllenamento(AllenamentoSyncDTO dto) {
        Optional<Allenamento> allenamentoEsistente = allenamentoRepository.findByClientUuid(dto.getClientUuid());
        
        Allenamento allenamento;
        if (allenamentoEsistente.isPresent()) {
            allenamento = allenamentoEsistente.get();
            allenamento.setDataFine(dto.getDataFine());
            allenamento.setStato(dto.getStato());
        } else {
            allenamento = new Allenamento();
            allenamento.setClientUuid(dto.getClientUuid());
            allenamento.setDataInizio(dto.getDataInizio());
            allenamento.setDataFine(dto.getDataFine());
            allenamento.setStato(dto.getStato());
            
            Utente utente = utenteRepository.findById(dto.getIdUtente())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
            allenamento.setUtente(utente);
            
            if (dto.getIdGiorno() != null) {
                SchedaGiorno giorno = schedaGiornoRepository.findById(dto.getIdGiorno()).orElse(null);
                allenamento.setGiorno(giorno);
            }
        }
        
        allenamento = allenamentoRepository.save(allenamento);

        if (dto.getSerie() != null) {
            for (SerieSyncDTO serieDto : dto.getSerie()) {
                Optional<SerieLog> serieEsistente = serieLogRepository.findByClientUuid(serieDto.getClientUuid());
                
                if (serieEsistente.isEmpty()) {
                    SerieLog nuovaSerie = new SerieLog();
                    nuovaSerie.setClientUuid(serieDto.getClientUuid());
                    nuovaSerie.setNumeroSerie(serieDto.getNumeroSerie());
                    nuovaSerie.setRipetizioni(serieDto.getRipetizioni());
                    nuovaSerie.setCaricoKg(serieDto.getCaricoKg());
                    nuovaSerie.setRecuperoEffettivoSec(serieDto.getRecuperoEffettivoSec());
                    nuovaSerie.setTimestampEsecuzione(serieDto.getTimestampEsecuzione());
                    nuovaSerie.setAllenamento(allenamento);
                    
                    Esercizio esercizio = esercizioRepository.findById(serieDto.getIdEsercizio())
                            .orElseThrow(() -> new RuntimeException("Esercizio non trovato"));
                    nuovaSerie.setEsercizio(esercizio);
                    
                    serieLogRepository.save(nuovaSerie);
                }
            }
        }
        
        return "Sync completato per allenamento: " + allenamento.getClientUuid();
    }
}