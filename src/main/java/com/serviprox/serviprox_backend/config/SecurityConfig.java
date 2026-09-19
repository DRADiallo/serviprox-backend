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
                        .requestMatchers("/api/agent/**")
                        .hasAnyRole(
                                "LIVREUR", "AGENT",
                                "PRESTATAIRE", "ADMIN")

                        // ══ CLIENT ══
                        .requestMatchers("/api/client/**")
                        .hasAnyRole("CLIENT", "ADMIN")

                        // ══ TOUS AUTHENTIFIÉS ══
                        .anyRequest().authenticated()
                )

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
//package com.serviprox.serviprox_backend.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http)
//            throws Exception {
//        http
//                // Désactiver CSRF — API REST stateless
//                .csrf(csrf -> csrf.disable())
//
//                // Désactiver formulaire de login
//                // par défaut de Spring Security
//                .formLogin(AbstractHttpConfigurer::disable)
//
//                // Désactiver HTTP Basic Auth
//                // par défaut de Spring Security
//                .httpBasic(AbstractHttpConfigurer::disable)
//
//                // Pas de session — JWT stateless
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(
//                                SessionCreationPolicy.STATELESS))
//
//                // ← AJOUT : activer CORS depuis CorsConfig.java
//                // Lit automatiquement le Bean CorsFilter défini
//                // dans CorsConfig → autorise Flutter Web (Chrome)
//                .cors(Customizer.withDefaults())
//
//                .authorizeHttpRequests(auth -> auth
//
//                        // ══ PREFLIGHT CORS — OPTIONS ══
//                        // ← AJOUT CRITIQUE : autoriser les requêtes
//                        // OPTIONS envoyées par le navigateur Chrome
//                        // AVANT chaque vrai appel API (préflight CORS)
//                        // Sans cette ligne → 403 sur toutes les requêtes
//                        // Flutter Web même si le endpoint est public !
//                        .requestMatchers(HttpMethod.OPTIONS, "/**")
//                        .permitAll()
//
//                        // ══ PUBLICS — sans token ══
//                        // Inscription et connexion
//                        .requestMatchers("/api/auth/**")
//                        .permitAll()
//
//                        // Un client doit voir les prestataires !
//                        .requestMatchers(
//                                "/api/prestataire",
//                                "/api/prestataire/zone",
//                                "/api/prestataire/tous")
//                        .authenticated()
//
//                        // ══ ADMIN ══
//                        // Toute la supervision React
//                        // Dashboard, validation, stats
//                        .requestMatchers("/api/admin/**")
//                        .hasRole("ADMIN")
//
//                        // ══ PRESTATAIRE ══
//                        // Gestion service, agents,
//                        // commandes, config, devis
//                        .requestMatchers("/api/prestataire/**")
//                        .hasAnyRole("PRESTATAIRE", "ADMIN")
//
//                        // ══ AGENT/LIVREUR ══
//                        // Interventions, affectations,
//                        // position GPS, preuves, bons
//                        .requestMatchers("/api/agent/**")
//                        .hasAnyRole(
//                                "LIVREUR", "AGENT",
//                                "PRESTATAIRE", "ADMIN")
//
//                        // ══ CLIENT ══
//                        // Commandes, devis, paiements,
//                        // évaluations, litiges, notifs
//                        .requestMatchers("/api/client/**")
//                        .hasAnyRole("CLIENT", "ADMIN")
//
//                        // ══ TOUS AUTHENTIFIÉS ══
//                        // Endpoints partagés
//                        .anyRequest().authenticated()
//                )
//
//                // Filtre JWT avant authentification
//                .addFilterBefore(
//                        jwtAuthFilter,
//                        UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
//------------------------------------------------------ dernier recent-----------------------


//package com.serviprox.serviprox_backend.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//    private final JwtAuthFilter jwtAuthFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
//        http
//                // Désactiver CSRF — API REST stateless
//                .csrf(csrf->csrf.disable())
//
//                // Désactiver formulaire de login
//                // par défaut de Spring Security
//                .formLogin(AbstractHttpConfigurer::disable)
//
//                // Désactiver HTTP Basic Auth
//                // par défaut de Spring Security
//                .httpBasic(AbstractHttpConfigurer::disable)
//
//                // Pas de session — JWT stateless
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .cors(Customizer.withDefaults())
//
//                .authorizeHttpRequests(auth->auth
//                        // ══ PUBLICS — sans token ══
//                        // Inscription et connexion
//                        .requestMatchers("/api/auth/**")
//                        .permitAll()
//
//                        // Un client doit voir les prestataires !
//                        .requestMatchers(
//                                "/api/prestataire",
//                                "/api/prestataire/zone",
//                                "/api/prestataire/tous")
//                        .authenticated()
//                       // .hasAnyRole("CLIENT", "PRESTATAIRE", "ADMIN")
//
//                        // ══ ADMIN ══
//                        // Toute la supervision React
//                        // Dashboard, validation, stats
//                        .requestMatchers("/api/admin/**")
//                        .hasRole("ADMIN")
//
//                            // ══ PRESTATAIRE ══
//                            // Gestion service, agents,
//                            // commandes, config, devis
//
//                        .requestMatchers("/api/prestataire/**")
//                        .hasAnyRole("PRESTATAIRE","ADMIN")
//
//                        // ══ AGENT/LIVREUR ══
//                        // Interventions, affectations,
//                        // position GPS, preuves, bons
//
//                        .requestMatchers("/api/agent/**")
//                        .hasAnyRole("LIVREUR", "AGENT","PRESTATAIRE","ADMIN")
//
//                        // ══ CLIENT ══
//                        // Commandes, devis, paiements,
//                        // évaluations, litiges, notifs
//
//                        .requestMatchers("/api/client/**")
//                        .hasAnyRole("CLIENT","ADMIN")
//
//
//                        // ══ TOUS AUTHENTIFIÉS ══
//                        // Endpoints partagés
//                        .anyRequest().authenticated()
//
//                )
//                 // Filtre JWT avant authentification
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
//
//}
