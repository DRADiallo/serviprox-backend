package com.serviprox.serviprox_backend.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Devis — estimation financière
 * avant toute réparation
 */
@Data
public class DevisDTO {
    private Long id;
    private Long commandeId;
    private Double montantEstime;
    private String statut;
    private LocalDateTime dateGeneration;
    private LocalDateTime dateValidation;
}
