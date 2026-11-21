package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Scheda;
import com.riccioproductions.gymtracker.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SchedaRepository extends JpaRepository<Scheda, Long> {
    List<Scheda> findByUtenteIdOrderByIdAsc(Long utenteId);

    // Per le schermate: l'ultima scheda inserita compare in cima
    List<Scheda> findByUtenteIdOrderByIdDesc(Long utenteId);

    Optional<Scheda> findByIdAndUtenteId(Long id, Long utenteId);

    // Migrazione: i dati creati prima del multiutente passano al primo utente registrato
    @Modifying
    @Query("update Scheda s set s.utente = :utente where s.utente is null")
    int assegnaOrfane(@Param("utente") Utente utente);
}
