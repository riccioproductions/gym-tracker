package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class SerieEseguita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "allenamento_id", nullable = false)
    private Allenamento allenamento;

    @ManyToOne
    @JoinColumn(name = "esercizio_giorno_id", nullable = false)
    private EsercizioGiorno esercizioGiorno;

    private Integer numeroSerie;
    private Integer ripetizioniEffettuate;
    private Double caricoEffettivo;
    private LocalDateTime timestampCompletamento;
}