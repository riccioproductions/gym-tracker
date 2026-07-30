package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Allenamento;
import com.riccioproductions.gymtracker.model.StatoAllenamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AllenamentoRepository extends JpaRepository<Allenamento, Long> {
    // fondamentale per il requisito "riaprire l'app e ritrovare l'allenamento in corso"
    Optional<Allenamento> findFirstByStatoOrderByDataInizioDesc(StatoAllenamento stato);

    List<Allenamento> findByGiornoIdOrderByDataInizioDesc(Long giornoId);
}