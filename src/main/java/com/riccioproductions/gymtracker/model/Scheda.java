package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Scheda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    private LocalDate dataCreazione = LocalDate.now();

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @OneToMany(mappedBy = "scheda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Giorno> giorni = new ArrayList<>();
}
