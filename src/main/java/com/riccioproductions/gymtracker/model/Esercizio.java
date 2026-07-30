package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Esercizio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String gruppoMuscolare; // facoltativo, es. "Petto", "Gambe"
}