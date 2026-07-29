package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.dto.SerieStoricoDTO;
import com.riccioproductions.gymtracker.entity.SerieLog;
import com.riccioproductions.gymtracker.repository.SerieLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticheService {

    private final SerieLogRepository serieLogRepository;

    public StatisticheService(SerieLogRepository serieLogRepository) {
        this.serieLogRepository = serieLogRepository;
    }

    public List<SerieStoricoDTO> getStoricoEsercizio(Long idUtente, Long idEsercizio) {
        List<SerieLog> logEseguiti = serieLogRepository
                .findByEsercizio_IdEsercizioAndAllenamento_Utente_IdUtenteOrderByTimestampEsecuzioneAsc(idEsercizio, idUtente);

        return logEseguiti.stream().map(log -> {
            SerieStoricoDTO dto = new SerieStoricoDTO();
            dto.setData(log.getTimestampEsecuzione());
            dto.setCaricoKg(log.getCaricoKg());
            dto.setRipetizioni(log.getRipetizioni());
            dto.setNumeroSerie(log.getNumeroSerie());           
            BigDecimal rep = new BigDecimal(log.getRipetizioni());
            dto.setVolumeTotale(log.getCaricoKg().multiply(rep));
            
            return dto;
        }).collect(Collectors.toList());
    }
}