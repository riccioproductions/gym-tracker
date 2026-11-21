package com.riccioproductions.gymtracker.repository;

import com.riccioproductions.gymtracker.model.PushSottoscrizione;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PushSottoscrizioneRepository extends JpaRepository<PushSottoscrizione, Long> {
    List<PushSottoscrizione> findByUtenteId(Long utenteId);
    Optional<PushSottoscrizione> findByEndpoint(String endpoint);
    void deleteByEndpointAndUtenteId(String endpoint, Long utenteId);
}
