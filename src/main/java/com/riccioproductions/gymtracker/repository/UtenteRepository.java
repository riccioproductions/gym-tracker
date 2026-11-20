package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
    boolean existsByUsername(String username);
}
