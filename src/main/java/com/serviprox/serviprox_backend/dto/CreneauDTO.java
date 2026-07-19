package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Créneau — plage horaire planifiée
 * Obligatoire pour les soins à domicile
 */
@Data
public class CreneauDTO {
    private Long id;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String statut;
}
