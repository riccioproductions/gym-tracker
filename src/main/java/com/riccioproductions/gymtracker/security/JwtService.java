package com.riccioproductions.gymtracker.security;

import com.riccioproductions.gymtracker.model.Utente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

// Crea e verifica i token di accesso (JWT firmati HS256). Nel token c'è solo l'id dell'utente.
@Service
public class JwtService {

    private final SecretKey chiave;
    private final long durataMs;

    public JwtService(@Value("${gymtracker.jwt.segreto:}") String segreto,
                      @Value("${gymtracker.jwt.durata-giorni:30}") long durataGiorni) {
        if (segreto == null || segreto.length() < 32) {
            throw new IllegalStateException(
                    "Manca il segreto JWT: imposta la variabile d'ambiente GYM_JWT_SEGRETO (almeno 32 caratteri casuali).");
        }
        this.chiave = Keys.hmacShaKeyFor(segreto.getBytes(StandardCharsets.UTF_8));
        this.durataMs = durataGiorni * 24L * 60 * 60 * 1000;
    }

    public String genera(Utente utente) {
        Date ora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(utente.getId()))
                .issuedAt(ora)
                .expiration(new Date(ora.getTime() + durataMs))
                .signWith(chiave)
                .compact();
    }

    /** Id dell'utente se il token è valido e non scaduto, altrimenti vuoto. */
    public Optional<Long> leggiUtenteId(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chiave).build().parseSignedClaims(token).getPayload();
            return Optional.of(Long.valueOf(c.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
