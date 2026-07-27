package com.serviprox.serviprox_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // Désactiver CSRF — API REST stateless
                .csrf(csrf->csrf.disable())

                // Pas de session — JWT stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth->auth
                        // ══ PUBLICS — sans token ══
                        // Inscription et connexion
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // ══ ADMIN ══
                        // Toute la supervision React
                        // Dashboard, validation, stats
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                            // ══ PRESTATAIRE ══
                            // Gestion service, agents,
                            // commandes, config, devis
                        .requestMatchers("/api/prestataire/**")
                        .hasAnyRole("PRESTATAIRE","ADMIN")

                        // ══ AGENT/LIVREUR ══
                        // Interventions, affectations,
                        // position GPS, preuves, bons
                        .requestMatchers("/api/agent/**")
                        .hasAnyRole("LIVREUR", "AGENT","PRESTAITAIRE","ADMIN")
                        // ══ CLIENT ══
                        // Commandes, devis, paiements,
                        // évaluations, litiges, notifs
                        .requestMatchers("/api/client/**")
                        .hasAnyRole("CLIENT","ADMIN")
                        // ══ TOUS AUTHENTIFIÉS ══
                        // Endpoints partagés
                        .anyRequest().authenticated()

                )
                 // Filtre JWT avant authentification
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
