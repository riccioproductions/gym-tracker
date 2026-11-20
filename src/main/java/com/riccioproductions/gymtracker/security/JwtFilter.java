package com.riccioproductions.gymtracker.security;

import com.riccioproductions.gymtracker.repository.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// Legge "Authorization: Bearer <token>" e, se valido, autentica la richiesta con l'entità Utente come principal.
// Non è un @Component: la crea SecurityConfig, così non viene registrata due volte.
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtenteRepository utenti;

    public JwtFilter(JwtService jwtService, UtenteRepository utenti) {
        this.jwtService = jwtService;
        this.utenti = utenti;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.leggiUtenteId(header.substring(7))
                    .flatMap(utenti::findById) // un utente eliminato non entra più, anche con token valido
                    .ifPresent(u -> SecurityContextHolder.getContext()
                            .setAuthentication(new UsernamePasswordAuthenticationToken(u, null, List.of())));
        }
        chain.doFilter(request, response);
    }
}
