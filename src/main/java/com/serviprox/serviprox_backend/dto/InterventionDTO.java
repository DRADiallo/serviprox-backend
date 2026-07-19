package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Intervention — exécution terrain
 */
@Data
public class InterventionDTO {
    private Long id;
    private Long commandeId;
    private String adresseDepart;
    private String adresseArrivee;
    private String typeIntervention;
    private String positionActuelle;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
