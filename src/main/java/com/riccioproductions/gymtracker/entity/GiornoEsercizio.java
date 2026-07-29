package com.riccioproductions.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "giorno_esercizi")
@Data
@NoArgsConstructor
public class GiornoEsercizio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giorno_esercizio")
    private Long idGiornoEsercizio;

    @Column(nullable = false)
    private Integer ordine = 0;

    @Column(name = "serie_target", nullable = false)
    private Integer serieTarget = 3;

    @Column(name = "ripetizioni_target", length = 20)
    private String ripetizioniTarget = "8-12"; // Può essere testuale, es. "AMRAP" o "5"

    @Column(name = "recupero_secondi", nullable = false)
    private Integer recuperoSecondi = 90;

    @Column(length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_giorno", nullable = false)
    private SchedaGiorno giorno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_esercizio", nullable = false)
    private Esercizio esercizio;
}