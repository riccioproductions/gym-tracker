package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.*;

@Entity
@Data
public class Scheda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome della scheda è obbligatorio.")
    @Size(max = 100, message = "Il nome della scheda può avere al massimo 100 caratteri.")
    @Column(nullable = false)
    private String nome;

    @Size(max = 255, message = "La descrizione può avere al massimo 255 caratteri.")
    private String descrizione;

    private LocalDate dataCreazione = LocalDate.now();

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @OneToMany(mappedBy = "scheda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Giorno> giorni = new ArrayList<>();
}
