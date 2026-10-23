package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * ═══════════════════════════════════════════════════════════════════
 * COUCHE 2 — MÉTAMODÈLE DE CONFIGURATION (inspiré openEHR)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Principe fondamental de l'architecture du mémoire :
 * "Le noyau NE teste JAMAIS typeService avec des if/else.
 *  Il LIT dynamiquement depuis la ConfigurationService."
 *
 * Cette entité EST l'archétype du service.
 * Elle configure le comportement du noyau SANS modifier le code.
 *
 * Exemple : ajouter "jardinage" = créer 1 ligne en base via React.
 * L'application Flutter l'affiche IMMÉDIATEMENT. Zéro ligne de code.
 *
 * ───────────────────────────────────────────────────────────────────
 * Correspondance Tableau 15 du mémoire (Chapitre 3) :
 * ┌──────────────────┬──────────────────────────────────────────────┐
 * │ typeService      │ Identifiant unique du type de service        │
 * │ labelAffiche     │ Libellé affiché dans Flutter                 │
 * │ avecAgent        │ Nécessite un agent collecteur/livreur ?      │
 * │ etapes           │ JSON — workflow spécifique au service        │
 * │ champsCommande   │ JSON — formulaire dynamique de commande      │
 * │ typesPrestation  │ JSON — catégories de prestations             │
 * │ exigeDevis       │ Devis obligatoire avant confirmation ?       │
 * │ exigePreuve      │ Photo/preuve requise pour clôturer ?         │
 * │ actif            │ Service visible dans Flutter ?               │
 * └──────────────────┴──────────────────────────────────────────────┘
 */
@Entity
@Data
@Table(name = "configurations_service")
public class ConfigurationService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── IDENTIFIANT MÉTIER ──────────────────────────────────────────
    // Clé unique : "pressing", "reparation", "soins"
    // L'admin peut créer "jardinage", "demenagement"... via React
    // sans modifier une seule ligne de code Java.
    @Column(nullable = false, unique = true)
    private String typeService;

    // ─── LIBELLÉ FLUTTER ────────────────────────────────────────────
    // Ce que le client voit dans l'application mobile.
    // Ex: "Service de Pressing", "Réparation Équipements"
    @Column
    private String labelAffiche;

    // ─── MODE AGENT ─────────────────────────────────────────────────
    // true  → pressing (un agent collecte et livre à domicile)
    // false → reparation / soins (le prestataire est contacté direct)
    // Ce booléen remplace tout if/else sur typeService dans le noyau.
    @Column
    private Boolean avecAgent = false;

    // ─── WORKFLOW MÉTIER (JSON) ──────────────────────────────────────
    // Les étapes SPÉCIFIQUES de ce service (Couche 2 configurable).
    // Ce sont les sous-étapes de EN_COURS dans StatutCommande.
    //
    // pressing   : ["collecte", "traitement", "livraison"]
    // reparation : ["diagnostic", "reparation", "restitution"]
    // soins      : ["reservation", "intervention", "validation"]
    //
    // ⚠️  Les statuts UNIVERSELS restent dans l'enum StatutCommande
    //     (CREE, CONFIRMEE, EN_COURS, CLOTUREE, ANNULEE, LITIGE)
    //     Ces étapes-ci sont les jalons MÉTIER à l'intérieur de EN_COURS.
    @Column(columnDefinition = "TEXT")
    private String etapes;

    // ─── FORMULAIRE DE COMMANDE (JSON) ──────────────────────────────
    // Champs du formulaire de création de commande.
    // Flutter construit le formulaire DYNAMIQUEMENT depuis ce JSON.
    // Plus aucun widget codé en dur pour chaque type de service !
    //
    // Structure de chaque champ :
    // {
    //   "code"     : identifiant technique (ex: "modeDepot"),
    //   "label"    : libellé affiché (ex: "Mode de dépôt"),
    //   "type"     : TEXT | NUMBER | LIST | DATE | TIME,
    //   "valeurs"  : [... options si type=LIST ...],
    //   "requis"   : true | false
    // }
    //
    // Exemple pressing — champsCommande JSON :
    // [
    //   {"code":"typeArticle", "label":"Type d'article", "type":"LIST",
    //    "valeurs":["Chemise","Pantalon","Robe","Boubou/Bazin","Darape"],
    //    "requis":true},
    //   {"code":"quantite", "label":"Quantité", "type":"NUMBER",
    //    "requis":true},
    //   {"code":"modeDepot", "label":"Mode de dépôt", "type":"LIST",
    //    "valeurs":["Collecte domicile","Dépôt atelier (livraison)",
    //               "Dépôt et récupération"],
    //    "requis":true},
    //   {"code":"descriptionColis","label":"Précisions","type":"TEXT",
    //    "requis":false}
    // ]
    //
    // ✅ L'enum ModeDepot Java est SUPPRIMÉ.
    //    Les valeurs viennent de l'API → Flutter les lit dynamiquement.
    //    Ajouter un mode = modifier le JSON, pas le code Java.
    @Column(columnDefinition = "TEXT")
    private String champsCommande;

    // ─── TYPES DE PRESTATION (JSON) ─────────────────────────────────
    // Catégories proposées par ce type de service.
    // Sert à afficher le menu de sélection dans Flutter.
    //
    // pressing   : [{"nom":"Lavage kilo"},{"nom":"Nettoyage sec"},...]
    // reparation : [{"nom":"Téléphone"},{"nom":"PC/Tablette"},...]
    // soins      : [{"nom":"Coiffure"},{"nom":"Massage"},...]
    @Column(columnDefinition = "TEXT")
    private String typesPrestation;

    // ─── CONTRAINTE DEVIS ────────────────────────────────────────────
    // true  → le prestataire envoie un devis avant confirmation
    //         (typique : réparation, déménagement)
    // false → commande directe sans devis (pressing, soins simples)
    @Column
    private Boolean exigeDevis = false;

    // ─── CONTRAINTE PREUVE ───────────────────────────────────────────
    // true  → photo/preuve requise pour clôturer la commande
    //         (pressing : photos avant/après, réparation : photos réparées)
    @Column
    private Boolean exigePreuve = true;

    // ─── VISIBILITÉ ──────────────────────────────────────────────────
    // false → masqué dans Flutter (service temporairement indisponible)
    // L'admin active/désactive via React sans toucher au code.
    @Column
    private Boolean actif = true;
}