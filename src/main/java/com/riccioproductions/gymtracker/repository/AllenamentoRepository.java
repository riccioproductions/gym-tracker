package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Allenamento;
import com.riccioproductions.gymtracker.model.StatoAllenamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AllenamentoRepository extends JpaRepository<Allenamento, Long> {

    Optional<Allenamento> findFirstByUtenteIdAndStatoOrderByDataInizioDesc(Long utenteId, StatoAllenamento stato);

    Optional<Allenamento> findByIdAndUtenteId(Long id, Long utenteId);

    List<Allenamento> findByUtenteIdAndGiornoIdOrderByDataInizioDesc(Long utenteId, Long giornoId);

    List<Allenamento> findByUtenteIdOrderByDataInizioDesc(Long utenteId, Pageable pageable);

    List<Allenamento> findByUtenteId(Long utenteId, Sort sort);
}
