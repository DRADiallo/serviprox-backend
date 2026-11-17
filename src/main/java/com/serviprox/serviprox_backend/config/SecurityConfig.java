package com.serviprox.serviprox_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // Désactiver CSRF — API REST stateless
                .csrf(csrf -> csrf.disable())

                // Désactiver formulaire de login
                .formLogin(AbstractHttpConfigurer::disable)

                // Désactiver HTTP Basic Auth
                .httpBasic(AbstractHttpConfigurer::disable)

                // Pas de session — JWT stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                // CORS — utilise CorsConfig.java
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth

                        // OPTIONS — preflight CORS
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // ══ PUBLICS — sans token ══
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Liste prestataires — authentifié
                        .requestMatchers(
                                "/api/prestataire",
                                "/api/prestataire/zone",
                                "/api/prestataire/tous")
                        .authenticated()

                        // ══ ADMIN ══
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ══ PRESTATAIRE ══
                        .requestMatchers("/api/prestataire/**")
                        .hasAnyRole("PRESTATAIRE", "ADMIN")

                        // ══ AGENT/LIVREUR ══
                        .requestMatchers("/api/agents/**")
                        .hasAnyRole(
                                "LIVREUR", "AGENT",
                                "PRESTATAIRE", "ADMIN")

                        // ══ CLIENT ══
                        .requestMatchers("/api/client/**")
                        .hasAnyRole("CLIENT", "ADMIN")

                                // ══ COMMANDES ══
                                .requestMatchers("/api/commande/**")
                                .hasAnyRole(
                                        "CLIENT", "PRESTATAIRE",
                                        "LIVREUR", "ADMIN")

                        // ══ SERVICE CONFIG ══
                                .requestMatchers("/api/service-config/**")
                                .authenticated()

                        //  : PREUVES
                        .requestMatchers("/api/preuve/**")
                        .hasAnyRole(
                                "CLIENT", "PRESTATAIRE", "LIVREUR", "ADMIN")

                        // AJOUT POUR VRIFICATIONS
                        .requestMatchers("/api/verification/**")
                        .hasAnyRole("CLIENT", "PRESTATAIRE", "LIVREUR", "ADMIN")

                        // ← AJOUT après /api/verification/**
                        .requestMatchers("/api/notification/**")
                        .authenticated()

                        .requestMatchers("/api/paiement/**")
                        .hasAnyRole("CLIENT", "PRESTATAIRE","AGENT", "LIVREUR", "ADMIN")

                        // ══ TOUS AUTHENTIFIÉS ══
                        .anyRequest().authenticated()
                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/prestataire", "/api/prestataire/zone", "/api/prestataire/tous").authenticated()
//                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/prestataire/**").hasAnyAuthority("PRESTATAIRE", "ROLE_PRESTATAIRE", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/agents/**").hasAnyAuthority("LIVREUR", "ROLE_LIVREUR", "AGENT", "ROLE_AGENT", "PRESTATAIRE", "ROLE_PRESTATAIRE", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/client/**").hasAnyAuthority("CLIENT", "ROLE_CLIENT", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/commande/**").hasAnyAuthority("CLIENT", "ROLE_CLIENT", "PRESTATAIRE", "ROLE_PRESTATAIRE", "LIVREUR", "ROLE_LIVREUR", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/service-config/**").authenticated()
//                        .requestMatchers("/api/preuve/**").hasAnyAuthority("CLIENT", "ROLE_CLIENT", "PRESTATAIRE", "ROLE_PRESTATAIRE", "LIVREUR", "ROLE_LIVREUR", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/verification/**").hasAnyAuthority("CLIENT", "ROLE_CLIENT", "PRESTATAIRE", "ROLE_PRESTATAIRE", "LIVREUR", "ROLE_LIVREUR", "ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/notification/**").authenticated()
//                        .requestMatchers("/api/paiement/**").hasAnyAuthority("CLIENT", "ROLE_CLIENT", "PRESTATAIRE", "ROLE_PRESTATAIRE", "AGENT", "ROLE_AGENT", "LIVREUR", "ROLE_LIVREUR", "ADMIN", "ROLE_ADMIN")
//                        .anyRequest().authenticated()
//                )


                // Filtre JWT avant authentification
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

