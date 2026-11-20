package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EsercizioRepository extends JpaRepository<Esercizio, Long> {
    List<Esercizio> findByUtenteId(Long utenteId);
    Optional<Esercizio> findByIdAndUtenteId(Long id, Long utenteId);

    @Modifying
    @Query("update Esercizio e set e.utente = :utente where e.utente is null")
    int assegnaOrfani(@Param("utente") Utente utente);
}
