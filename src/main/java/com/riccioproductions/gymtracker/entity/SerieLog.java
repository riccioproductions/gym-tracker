package com.riccioproductions.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "serie_log")
@Data
@NoArgsConstructor
public class SerieLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_serie_log")
    private Long idSerieLog;

    @Column(name = "numero_serie", nullable = false)
    private Integer numeroSerie;

    @Column(nullable = false)
    private Integer ripetizioni;

    @Column(name = "carico_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal caricoKg = BigDecimal.ZERO; // Usiamo BigDecimal per evitare errori di arrotondamento sui pesi

    @Column(precision = 3, scale = 1)
    private BigDecimal rpe; // Rate of Perceived Exertion (opzionale)

    @Column(name = "recupero_effettivo_sec")
    private Integer recuperoEffettivoSec;

    @Column(name = "timestamp_esecuzione", nullable = false)
    private LocalDateTime timestampEsecuzione;

    @Column(name = "client_uuid", nullable = false, unique = true, length = 36)
    private String clientUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_allenamento", nullable = false)
    private Allenamento allenamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_esercizio", nullable = false)
    private Esercizio esercizio;
}