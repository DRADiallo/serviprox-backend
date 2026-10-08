package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeDTO {
    private Long id;
    private String statut;
    private Double montantTotal;
    private LocalDate dateCommande;
    private String motDepot;
    private String nomPrestataire;
    private String nomClient;
    private String telephoneClient;
    private Long agentCollecteId;
    private String nomAgentCollecte;
    private String telephoneAgentCollecte;
    private Long agentLivraisonId;
    private String nomAgentLivraison;
    private String telephoneAgentLivraison;
    private String descriptionColis;
    private List<LigneCommandeDTO>lignes;

}
