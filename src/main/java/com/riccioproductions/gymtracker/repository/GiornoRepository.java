package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Giorno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GiornoRepository extends JpaRepository<Giorno, Long> {
    List<Giorno> findBySchedaIdOrderByOrdineAsc(Long schedaId);
}