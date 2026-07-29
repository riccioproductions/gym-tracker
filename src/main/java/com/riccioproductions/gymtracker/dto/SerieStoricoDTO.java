package com.riccioproductions.gymtracker.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SerieStoricoDTO {
    private LocalDateTime data;
    private BigDecimal caricoKg;
    private Integer ripetizioni;
    private Integer numeroSerie;
    private BigDecimal volumeTotale; 
}