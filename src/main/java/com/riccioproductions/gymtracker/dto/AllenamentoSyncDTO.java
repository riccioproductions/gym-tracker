package com.riccioproductions.gymtracker.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AllenamentoSyncDTO {
    private String clientUuid;
    private Long idUtente; 
    private Long idGiorno; 
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private String stato; 
    
    private List<SerieSyncDTO> serie; 
}