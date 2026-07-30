package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EsercizioGiornoRepository extends JpaRepository<EsercizioGiorno, Long> {
    List<EsercizioGiorno> findByGiornoIdOrderByOrdineAsc(Long giornoId);
}