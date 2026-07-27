package com.serviprox.serviprox_backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
//import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;


/**
 * Utilitaire JWT — génère et valide les tokens
 *
 * Un token JWT contient :
 * - userId   : identifiant de l'utilisateur
 * - role     : CLIENT, PRESTATAIRE, LIVREUR, ADMIN
 * - expiration : durée de validité (24h)
 *
 * Format : xxxxx.yyyyy.zzzzz
 * Header.Payload.Signature
 */

@Component
public class JwtUtils {

    // Clé secrète définie dans application.properties
    // Minimum 256 bits pour HS256
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Durée de validité du token : 24h
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // ═══ GÉNÉRER UN TOKEN ═══
    // Appelé après login ou inscription réussie
    // Flutter stocke ce token et l'envoie
    // dans chaque requête suivante

    public String genererToken(Long userId, String role){

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
         return Jwts.builder()
                 //Qui est l'utilisateur
                 .setSubject(userId.toString())
                 //Son role dans l application
                 .claim("role",role)
                 // Quand le token a été cree
                 .setIssuedAt(new Date())
                // Quand le token expire
                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                 // Algorithme de signature
                 .signWith(key, SignatureAlgorithm.HS256)
                 .compact();
    }

    // ═══ EXTRAIRE USER ID ═══
    // Spring Security lit le userId
    // depuis le token pour identifier
    // l'utilisateur à chaque requête

    public Long getUserIdFromToken(String token){
        Claims claims= parseClaims(token);
        return  Long.parseLong(claims.getSubject());
    }

    // ═══ EXTRAIRE ROLE ═══
    // Spring Security lit le role
    // pour vérifier les droits d'accès
    // Ex: seul ADMIN peut accéder à /api/admin/*

    public String getRoleFromToken(String token){

        Claims claims = parseClaims(token);

        return  claims.get("role", String.class);
    }

    // ═══ VALIDER UN TOKEN ═══
    // Vérifie que le token :
    // 1. Est bien signé par notre clé secrète
    // 2. N'est pas expiré
    // 3. N'est pas malformé

    public boolean validateToken(String token){

        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expiré → utilisateur doit
            // se reconnecter
            throw new RuntimeException("Token expiré —"+ "veuillez vous reconnecter");
        } catch (JwtException e) {
            throw new RuntimeException("Token invalide");
        }
    }

    // ═══ MÉTHODE PRIVÉE ═══
    // Parse le token et retourne les claims
    private Claims parseClaims(String token){

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }





}
