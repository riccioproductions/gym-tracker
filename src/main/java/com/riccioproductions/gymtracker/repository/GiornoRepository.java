package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.Giorno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GiornoRepository extends JpaRepository<Giorno, Long> {
    List<Giorno> findBySchedaIdOrderByOrdineAsc(Long schedaId);

    // Il proprietario del giorno è il proprietario della sua scheda
    Optional<Giorno> findByIdAndSchedaUtenteId(Long id, Long utenteId);
}
