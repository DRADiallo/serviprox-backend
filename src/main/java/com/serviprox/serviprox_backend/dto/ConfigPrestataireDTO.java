package com.serviprox.serviprox_backend.dto;

import lombok.Data;

/**
 * DTO ConfigPrestataire — configuration métier
 */

@Data
public class ConfigPrestataireDTO {

    private Long id;
    private Long prestataireId;
    private String nomPrestataire;
    private String typeService;

    // ─── Champs existants ────────────────────────────────
    private String tarifsPersonnalises;
    private String optionsActives;
    private String prestationsAjoutees;
    private String zoneCouverture;

    // ─── Champs ajoutés (Couche 2 — mémoire) ─────────────

    // Étapes personnalisées de ce prestataire
    // null → utilise les étapes du ConfigurationService
    // Ex: ["reception","traitement","restitution"]
    private String etapesPersonnalisees;

    // Modes de dépôt que ce prestataire accepte
    // null → tous les modes acceptés
    // Ex: ["Collecte domicile","Dépôt atelier (livraison)"]
    private String modesDepotAcceptes;

    // Ce pressing utilise-t-il des agents ?
    // null → hérite de ConfigurationService.avecAgent
    private Boolean avecAgent;

    // Disponible pour nouvelles commandes ?
    private Boolean disponible;

    // À ajouter — viennent de ConfigurationService
    private String champsCommande;    // JSON des champs du formulaire
    private String typesPrestation;   // JSON des prestations proposées
    private String etapesEffectives;  // étapesPersonnalisees OU ConfigurationService.etapes
}
