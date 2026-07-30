package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Scheda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedaRepository extends JpaRepository<Scheda, Long> {
}