package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.GiornoEsercizio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiornoEsercizioRepository extends JpaRepository<GiornoEsercizio, Long> {
    
    List<GiornoEsercizio> findByGiorno_IdGiornoOrderByOrdineAsc(Long idGiorno);
}