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

    // ─── modeDepot (était "motDepot" — corrigé) ─────────────────────────
    private String modeDepot;

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
    private List<LigneCommandeDTO> lignes;

    // Prix
    private Double prixParTypePrestations;
    private Double prixCourseCollecte;
    private Double prixCourseLivraison;
    private String detailPrixReel;
    private Boolean prixAccepteClient;

    // ─── ÉTAPE COURANTE (Couche 2 — workflow métier) ─────────────────────
    // null si statut = CREE ou CONFIRMEE
    // Ex pressing : "collecte" | "traitement" | "livraison"
    private String etapeCourante;
}