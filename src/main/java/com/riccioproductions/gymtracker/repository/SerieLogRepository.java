package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.SerieLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerieLogRepository extends JpaRepository<SerieLog, Long> {
    
    // FONDAMENTALE PER LA PWA: trova una serie usando l'UUID generato dal telefono
    Optional<SerieLog> findByClientUuid(String clientUuid);
    
    // Serve per generare i grafici: trova tutte le serie di un esercizio per un dato utente
    List<SerieLog> findByEsercizio_IdEsercizioAndAllenamento_Utente_IdUtenteOrderByTimestampEsecuzioneAsc(Long idEsercizio, Long idUtente);
}