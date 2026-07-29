package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.entity.Allenamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllenamentoRepository extends JpaRepository<Allenamento, Long> {
    
    Optional<Allenamento> findByClientUuid(String clientUuid);
}