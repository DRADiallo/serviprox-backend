package com.serviprox.serviprox_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "config_prestataires")
@NoArgsConstructor
@AllArgsConstructor
public class ConfigPrestataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── EXISTANT — bon à garder ─────────────────────────
    @Column(columnDefinition = "TEXT")
    private String tarifsPersonnalises;    // JSON

    @Column(columnDefinition = "TEXT")
    private String optionsActives;         // JSON

    @Column(columnDefinition = "TEXT")
    private String prestationsAjoutees;    // JSON

    @Column(columnDefinition = "TEXT")
    private String zoneCouverture;         // JSON

    // ─── À AJOUTER — requis par le mémoire ───────────────

    // Étapes personnalisées (sous-ensemble de ConfigurationService.etapes)
    // null → utilise les étapes du ConfigurationService
    // Ex pressing sans agent : ["reception","traitement","restitution"]
    @Column(columnDefinition = "TEXT")
    private String etapesPersonnalisees;

    // Modes de dépôt acceptés par CE prestataire
    // null → tous les modes du ConfigurationService acceptés
    // Ex : ["Collecte domicile","Dépôt atelier (livraison)"]
    @Column(columnDefinition = "TEXT")
    private String modesDepotAcceptes;

    // Surcharge avecAgent du ConfigurationService
    // null  → hérite de ConfigurationService.avecAgent
    // true  → ce pressing utilise des agents
    // false → ce pressing travaille sans agent
    @Column
    private Boolean avecAgent;

    // Disponibilité pour nouvelles commandes
    @Column
    private Boolean disponible = true;

    // ─── RELATIONS ────────────────────────────────────────
    @OneToOne
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;

    @ManyToOne
    @JoinColumn(name = "configuration_service_id")
    private ConfigurationService configurationService;
}
