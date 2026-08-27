package com.serviprox.serviprox_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Filtre JWT — intercepte chaque requête HTTP
 *
 * Pour chaque requête Flutter/React :
 * 1. Extraire le token du header Authorization
 * 2. Valider le token
 * 3. Identifier l'utilisateur
 * 4. Autoriser ou refuser l'accès
 *
 * Header attendu :
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */

@Component
@RequiredArgsConstructor
public class JwtAuthFilter  extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException,IOException{
       // Recuperer le Header Authorisation
       String authHeader = request.getHeader("Authorization");

        // Si pas de token → laisser passer
        // Spring Security gérera l'accès
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);
        // Extraire le token (sans "Bearer ")
        try {
            //Valider le token
            if (jwtUtils.validateToken(token)){

                // Extraire userId et role
                Long userdId= jwtUtils.getUserIdFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                // Créer l'authentification Spring
                // avec le rôle de l'utilisateur
                // ROLE_ préfixe requis par Spring
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userdId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_"+ role))
                );

                // Enregistrer dans le contexte
                // Spring Security reconnaît
                // l'utilisateur pour cette requête
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);
            }
        } catch (Exception e) {
            // token invalide -> reponse 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \""
                    + e.getMessage() + "\"}");
            return;
        }

        // Continuer vers le Controller
        filterChain.doFilter(request,response);
    }

}
