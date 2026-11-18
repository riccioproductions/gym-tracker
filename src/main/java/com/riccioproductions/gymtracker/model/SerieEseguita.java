package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class SerieEseguita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "allenamento_id", nullable = false)
    private Allenamento allenamento;

    @ManyToOne
    @JoinColumn(name = "esercizio_giorno_id", nullable = false)
    private EsercizioGiorno esercizioGiorno;
    
    private String nomeEsercizio;

    private Integer numeroSerie;

    // "SX" / "DX" per gli esercizi unilaterali, null per gli altri
    @Column(length = 2)
    private String lato;
    private Integer ripetizioniEffettuate;
    private Double caricoEffettivo;
    private LocalDateTime timestampCompletamento;
}