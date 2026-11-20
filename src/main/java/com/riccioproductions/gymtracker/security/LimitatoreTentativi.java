package com.riccioproductions.gymtracker.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

// Freno ai tentativi di accesso a forza bruta: troppi errori in 15 minuti per la stessa chiave => 429.
// Sta in memoria (si azzera al riavvio), basta e avanza per un'app piccola.
@Component
public class LimitatoreTentativi {

    private static final long FINESTRA_MS = 15 * 60 * 1000L;
    private final Map<String, Deque<Long>> falliti = new ConcurrentHashMap<>();

    public void verifica(String chiave, int massimo) {
        if (recenti(chiave) >= massimo) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Troppi tentativi. Riprova tra qualche minuto.");
        }
    }

    public void fallito(String chiave) {
        if (falliti.size() > 5000) falliti.keySet().removeIf(k -> recenti(k) == 0); // pulizia occasionale
        falliti.computeIfAbsent(chiave, k -> new ConcurrentLinkedDeque<>()).addLast(System.currentTimeMillis());
    }

    public void azzera(String chiave) {
        falliti.remove(chiave);
    }

    private int recenti(String chiave) {
        Deque<Long> d = falliti.get(chiave);
        if (d == null) return 0;
        long limite = System.currentTimeMillis() - FINESTRA_MS;
        while (true) {
            Long primo = d.peekFirst();
            if (primo == null || primo >= limite) break;
            d.pollFirst();
        }
        return d.size();
    }
}
