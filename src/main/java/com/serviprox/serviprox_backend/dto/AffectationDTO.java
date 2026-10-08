package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Affectation — lien agent-commande
 */
@Data
public class AffectationDTO {
    private Long id;
    private Long commandeId;
    private Long agentId;
    private String nomAgent;
    private LocalDateTime dateAffectation;

    private String statut;
    private Integer ordrePassage;
    private String zoneGeographique;

    private String typeAffectation;
    private String nomClient;
    private String telephoneClient;
    private String descriptionColis;
    private String motDepot;
    private Double montantTotal;
    private String nomPrestataire;


}
