package com.riccioproductions.gymtracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Invio email (SMTP). L'invio avviene in un thread a parte: la risposta all'utente è sempre rapida
// e non rivela se l'indirizzo esiste. Senza credenziali SMTP il servizio resta spento (si vede nel log).
@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender sender;
    private final String mittente;
    private final ExecutorService coda = Executors.newSingleThreadExecutor();

    public MailService(JavaMailSender sender, @Value("${spring.mail.username:}") String mittente) {
        this.sender = sender;
        this.mittente = mittente == null ? "" : mittente.trim();
        if (this.mittente.isEmpty()) {
            LOG.warn("Email NON configurate: mancano GYM_MAIL_UTENTE / GYM_MAIL_PASSWORD. Il recupero password non invierà nulla.");
        }
    }

    public boolean attivo() {
        return !mittente.isEmpty();
    }

    public void invia(String destinatario, String oggetto, String testo) {
        if (!attivo()) return;
        coda.submit(() -> {
            try {
                SimpleMailMessage m = new SimpleMailMessage();
                m.setFrom(mittente);
                m.setTo(destinatario);
                m.setSubject(oggetto);
                m.setText(testo);
                sender.send(m);
            } catch (Exception e) {
                LOG.warn("Invio email fallito: {}", e.toString());
            }
        });
    }
}
