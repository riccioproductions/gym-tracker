package com.riccioproductions.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schede_giorni")
@Data
@NoArgsConstructor
public class SchedaGiorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giorno")
    private Long idGiorno;

    @Column(name = "nome_giorno", nullable = false, length = 80)
    private String nomeGiorno;

    @Column(nullable = false)
    private Integer ordine = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_scheda", nullable = false)
    private Scheda scheda;
}