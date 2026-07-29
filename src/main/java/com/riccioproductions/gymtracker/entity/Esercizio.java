package com.riccioproductions.gymtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "esercizi")
@Data
@NoArgsConstructor
public class Esercizio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_esercizio")
    private Long idEsercizio;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "gruppo_muscolare", nullable = false, length = 50)
    private String gruppoMuscolare; // es: PETTO, SCHIENA, GAMBE

    @Column(length = 80)
    private String attrezzatura; // es: Bilanciere, Manubri, Corpo libero

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "url_video")
    private String urlVideo;

    // Relazione: Un utente può creare molti esercizi personalizzati (o NULL se di sistema)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente_creatore")
    private Utente utenteCreatore;
}