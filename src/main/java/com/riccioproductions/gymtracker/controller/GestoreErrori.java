package com.riccioproductions.gymtracker.controller;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class GestoreErrori {

    private static final Logger LOG = LoggerFactory.getLogger(GestoreErrori.class);

    private static ResponseEntity<Map<String, String>> risposta(HttpStatus stato, String messaggio) {
        return ResponseEntity.status(stato).body(Map.of("messaggio", messaggio));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> vincolo(DataIntegrityViolationException e) {
        String causa = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();
        if (causa.contains("unique") || causa.contains("duplicate")) {
            return risposta(HttpStatus.CONFLICT, "Esiste già un elemento con questo nome.");
        }
        if (causa.contains("data too long")) {
            return risposta(HttpStatus.BAD_REQUEST, "Uno dei testi inseriti è troppo lungo.");
        }
        if (causa.contains("cannot delete or update")) {
            return risposta(HttpStatus.CONFLICT,
                    "Impossibile eliminare: l'elemento è collegato ad altri dati (ad esempio allenamenti già registrati).");
        }
        LOG.warn("Violazione di integrità non classificata: {}", causa);
        return risposta(HttpStatus.BAD_REQUEST, "Dati non validi.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> conStato(ResponseStatusException e) {
        String msg = e.getReason() == null ? "Richiesta non valida" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("messaggio", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> nonValido(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .filter(m -> m != null && !m.isBlank())
                .findFirst().orElse("Dati non validi.");
        return risposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> vincoliBean(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst().orElse("Dati non validi.");
        return risposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoIllegibile(HttpMessageNotReadableException e) {
        return risposta(HttpStatus.BAD_REQUEST, "Dati non validi.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argomento(IllegalArgumentException e) {
        return risposta(HttpStatus.BAD_REQUEST, "Valore non valido.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> generico(RuntimeException e) {
        if (e.getClass() == RuntimeException.class) {
            String msg = e.getMessage() == null ? "Richiesta non valida" : e.getMessage();
            return risposta(HttpStatus.BAD_REQUEST, msg);
        }
        if (e instanceof ErrorResponse er) { 
            return ResponseEntity.status(er.getStatusCode()).body(Map.of("messaggio", "Richiesta non valida"));
        }
        LOG.error("Errore interno", e);
        return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno. Riprova più tardi.");
    }
}
