package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.*;

@Entity
@Data
@Table(name = "esercizio",
       uniqueConstraints = @UniqueConstraint(name = "uk_esercizio_utente_nome", columnNames = {"utente_id", "nome"}))
public class Esercizio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome dell'esercizio è obbligatorio.")
    @Size(max = 100, message = "Il nome dell'esercizio può avere al massimo 100 caratteri.")
    @Column(nullable = false)
    private String nome;

    @Size(max = 50, message = "Il gruppo muscolare può avere al massimo 50 caratteri.")
    private String gruppoMuscolare;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;
}
