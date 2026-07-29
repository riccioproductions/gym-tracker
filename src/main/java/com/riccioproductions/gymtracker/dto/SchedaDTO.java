package com.riccioproductions.gymtracker.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchedaDTO {
    private String nome;
    private String descrizione;
    private List<GiornoDTO> giorni;

    @Data
    public static class GiornoDTO {
        private String nomeGiorno;
        private List<EsercizioGiornoDTO> esercizi;
    }

    @Data
    public static class EsercizioGiornoDTO {
        private Long idEsercizio;
        private Integer serieTarget;
        private String ripetizioniTarget;
        private Integer recuperoSecondi;
        private String note;
    }
}