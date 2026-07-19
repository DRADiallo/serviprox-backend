package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO PreuveRealisation — photo/vidéo/signature
 * Système de double preuve du pressing
 */
@Data
public class PreuveRealisationDTO {
    private Long id;
    private Long commandeId;
    private String type;
    private String contenu;
    private LocalDateTime date;
}
