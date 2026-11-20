package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

// Il nome è univoco per utente (non più globale): due utenti possono avere entrambi "Panca piana".
@Entity
@Data
@Table(name = "esercizio",
       uniqueConstraints = @UniqueConstraint(name = "uk_esercizio_utente_nome", columnNames = {"utente_id", "nome"}))
public class Esercizio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String gruppoMuscolare;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;
}
