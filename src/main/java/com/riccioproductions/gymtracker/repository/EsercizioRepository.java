package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Esercizio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EsercizioRepository extends JpaRepository<Esercizio, Long> {
    List<Esercizio> findByUtenteId(Long utenteId);
    Optional<Esercizio> findByIdAndUtenteId(Long id, Long utenteId);
}
