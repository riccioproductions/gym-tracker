package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.Esercizio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EsercizioRepository extends JpaRepository<Esercizio, Long> {
    
    List<Esercizio> findByGruppoMuscolare(String gruppoMuscolare);
}