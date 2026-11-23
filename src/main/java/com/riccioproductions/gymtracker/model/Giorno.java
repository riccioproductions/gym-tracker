package com.riccioproductions.gymtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

@Entity
@Data
public class Giorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome del giorno è obbligatorio.")
    @Size(max = 100, message = "Il nome del giorno può avere al massimo 100 caratteri.")
    @Column(nullable = false)
    private String nome;

    @Min(value = 0, message = "L'ordine non è valido.")
    @Max(value = 1000, message = "L'ordine non è valido.")
    private Integer ordine;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "scheda_id", nullable = false)
    private Scheda scheda;

    @OneToMany(mappedBy = "giorno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EsercizioGiorno> esercizi = new ArrayList<>();
}