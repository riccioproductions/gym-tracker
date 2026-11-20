package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Data
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // sempre in minuscolo (vedi AuthService)
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @JsonIgnore
    @ToString.Exclude
    @Column(nullable = false)
    private String passwordHash;

    private LocalDateTime dataCreazione = LocalDateTime.now();
}
