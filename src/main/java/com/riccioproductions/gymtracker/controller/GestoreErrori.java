package com.riccioproductions.gymtracker.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

//Trasforma gli errori in risposte JSON {"messaggio": "..."} leggibili dal front end. 
@RestControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> vincolo(DataIntegrityViolationException e) {
        String causa = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();
        String msg = (causa.contains("unique") || causa.contains("duplicate"))
                ? "Esiste già un elemento con questo nome."
                : "Impossibile eliminare: l'elemento è collegato ad altri dati (ad esempio allenamenti già registrati).";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("messaggio", msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> generico(RuntimeException e) {
        String msg = e.getMessage() == null ? "Richiesta non valida" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("messaggio", msg));
    }
}
