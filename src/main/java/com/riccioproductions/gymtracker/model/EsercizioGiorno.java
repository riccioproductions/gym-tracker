package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

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

    @Min(value = 1, message = "Le serie devono essere almeno 1.")
    @Max(value = 100, message = "Troppe serie.")
    private Integer serie;

    @Min(value = 1, message = "Le ripetizioni target devono essere almeno 1.")
    @Max(value = 1000, message = "Ripetizioni target troppo alte.")
    private Integer ripetizioniTarget;

    @DecimalMin(value = "0", message = "Il carico non può essere negativo.")
    @DecimalMax(value = "1000", message = "Carico troppo alto.")
    private Double carico;

    @Size(max = 255, message = "Le note possono avere al massimo 255 caratteri.")
    private String note;

    @Min(value = 0, message = "L'ordine non è valido.")
    @Max(value = 1000, message = "L'ordine non è valido.")
    private Integer ordine;

    private Boolean unilaterale = false;
}