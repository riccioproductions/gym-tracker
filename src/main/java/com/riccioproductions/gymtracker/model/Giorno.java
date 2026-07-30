package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
public class Giorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome; // es. "Giorno A - Petto/Tricipiti"

    private Integer ordine;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "scheda_id", nullable = false)
    private Scheda scheda;

    @OneToMany(mappedBy = "giorno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EsercizioGiorno> esercizi = new ArrayList<>();
}