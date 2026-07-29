package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.Scheda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedaRepository extends JpaRepository<Scheda, Long> {
    
    List<Scheda> findByUtente_IdUtente(Long idUtente);
}