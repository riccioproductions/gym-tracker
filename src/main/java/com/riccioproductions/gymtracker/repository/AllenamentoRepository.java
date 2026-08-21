package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Allenamento;
import com.riccioproductions.gymtracker.model.StatoAllenamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AllenamentoRepository extends JpaRepository<Allenamento, Long> {

    Optional<Allenamento> findFirstByStatoOrderByDataInizioDesc(StatoAllenamento stato);

    List<Allenamento> findByGiornoIdOrderByDataInizioDesc(Long giornoId);

    List<Allenamento> findAllByOrderByDataInizioDesc(Pageable pageable);
}