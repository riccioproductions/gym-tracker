package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Allenamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "giorno_id", nullable = false)
    private Giorno giorno;

    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;

    @Enumerated(EnumType.STRING)
    private StatoAllenamento stato;

    @OneToMany(mappedBy = "allenamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SerieEseguita> serieEseguite = new ArrayList<>();
}