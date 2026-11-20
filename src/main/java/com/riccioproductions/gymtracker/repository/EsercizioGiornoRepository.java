package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.EsercizioGiorno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EsercizioGiornoRepository extends JpaRepository<EsercizioGiorno, Long> {
    List<EsercizioGiorno> findByGiornoIdOrderByOrdineAsc(Long giornoId);

    // Proprietario: giorno -> scheda -> utente
    Optional<EsercizioGiorno> findByIdAndGiornoSchedaUtenteId(Long id, Long utenteId);
}
