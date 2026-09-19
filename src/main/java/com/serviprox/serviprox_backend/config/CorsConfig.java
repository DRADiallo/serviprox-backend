package com.serviprox.serviprox_backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    // ← CHANGEMENT : CorsConfigurationSource au lieu de CorsFilter
    // Customizer.withDefaults() dans SecurityConfig cherche
    // un bean CorsConfigurationSource — pas CorsFilter !
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser Flutter Web (Chrome localhost)
        config.addAllowedOriginPattern("*");

        // Autoriser tous les headers
        config.addAllowedHeader("*");

        // Autoriser GET, POST, PUT, DELETE, OPTIONS...
        config.addAllowedMethod("*");

        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Appliquer à toutes les routes API
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
