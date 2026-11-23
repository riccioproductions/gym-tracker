package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Scheda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SchedaRepository extends JpaRepository<Scheda, Long> {
    List<Scheda> findByUtenteIdOrderByIdAsc(Long utenteId);

    List<Scheda> findByUtenteIdOrderByIdDesc(Long utenteId);

    Optional<Scheda> findByIdAndUtenteId(Long id, Long utenteId);
}
