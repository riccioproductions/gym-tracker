package com.riccioproductions.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "allenamenti")
@Data
@NoArgsConstructor
public class Allenamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_allenamento")
    private Long idAllenamento;

    @Column(name = "data_inizio", nullable = false)
    private LocalDateTime dataInizio;

    @Column(name = "data_fine")
    private LocalDateTime dataFine;

    @Column(nullable = false, length = 20)
    private String stato = "IN_CORSO"; // IN_CORSO, COMPLETATO, ABBANDONATO

    @Column(name = "client_uuid", nullable = false, unique = true, length = 36)
    private String clientUuid; // Generato dalla PWA per gestire la sincronizzazione offline

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_giorno")
    private SchedaGiorno giorno; // Nullable se fai un allenamento libero (fuori scheda)
}