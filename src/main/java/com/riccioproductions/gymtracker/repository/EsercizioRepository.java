package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Esercizio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsercizioRepository extends JpaRepository<Esercizio, Long> {
}