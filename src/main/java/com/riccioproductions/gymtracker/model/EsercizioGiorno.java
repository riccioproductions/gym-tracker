package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
public class EsercizioGiorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "giorno_id", nullable = false)
    private Giorno giorno;

    @ManyToOne
    @JoinColumn(name = "esercizio_id", nullable = false)
    private Esercizio esercizio;

    private Integer serie;
    private Integer ripetizioniTarget;
    private Double carico;
    private String note;
    private Integer ordine;
}