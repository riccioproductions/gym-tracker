package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.SerieEseguita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SerieEseguitaRepository extends JpaRepository<SerieEseguita, Long> {
    List<SerieEseguita> findByAllenamentoId(Long allenamentoId);
}