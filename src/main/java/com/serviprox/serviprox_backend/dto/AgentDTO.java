package com.serviprox.serviprox_backend.dto;

import lombok.Data;

/**
 * DTO Agent — données publiques de l'agent
 * sans informations sensibles
 */
@Data
public class AgentDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motoMatricule;
    private String competences;
    private Boolean disponibilite;
    private String positionActuelle;
    private Long prestataireId;
    private String statut;
    //  AJOUT : PIN retourné une seule fois
    // à la création pour que le prestataire
    // puisse le transmettre à l'agent
    private String codePinClair;
}
