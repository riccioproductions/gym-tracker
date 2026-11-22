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

    // Per il recupero della password. Minuscola. Null per gli account creati prima: la aggiungono da Impostazioni.
    @JsonIgnore
    @Column(unique = true, length = 254)
    private String email;

    @JsonIgnore
    @ToString.Exclude
    @Column(nullable = false)
    private String passwordHash;

    // Codice monouso inviato per email per reimpostare la password (solo hash) e sua scadenza
    @JsonIgnore
    @ToString.Exclude
    private String resetCodiceHash;

    @JsonIgnore
    private LocalDateTime resetScadenza;

    private LocalDateTime dataCreazione = LocalDateTime.now();
}
