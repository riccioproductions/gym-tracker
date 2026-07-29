package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.SchedaGiorno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchedaGiornoRepository extends JpaRepository<SchedaGiorno, Long> {
    
    // Recupera tutti i giorni di una specifica scheda ordinati
    List<SchedaGiorno> findByScheda_IdSchedaOrderByOrdineAsc(Long idScheda);
}