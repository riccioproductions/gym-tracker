package com.riccioproductions.gymtracker.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SerieSyncDTO {
    private String clientUuid;
    private Long idEsercizio;
    private Integer numeroSerie;
    private Integer ripetizioni;
    private BigDecimal caricoKg;
    private Integer recuperoEffettivoSec;
    private LocalDateTime timestampEsecuzione;
}