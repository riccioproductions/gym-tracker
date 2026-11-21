package com.riccioproductions.gymtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

// Un dispositivo (browser) di un utente che ha acconsentito alle notifiche push.
@Entity
@Data
@Table(name = "push_sottoscrizione",
       uniqueConstraints = @UniqueConstraint(name = "uk_push_endpoint", columnNames = "endpoint"))
public class PushSottoscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false, length = 600)
    private String endpoint;

    @Column(nullable = false, length = 200)
    private String p256dh;

    @Column(nullable = false, length = 100)
    private String auth;
}
