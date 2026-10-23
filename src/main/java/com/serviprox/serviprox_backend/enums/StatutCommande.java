package com.serviprox.serviprox_backend.enums;

public enum StatutCommande {
    CREE,
    CONFIRMEE,
    EN_COURS,
    CLOTUREE,
    ANNULEE,
    LITIGE
}




/**
 * ═══════════════════════════════════════════════════════════════════
 * COUCHE 1 — MODÈLE DE RÉFÉRENCE STABLE (inspiré openEHR)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Ces 6 statuts sont UNIVERSELS : ils s'appliquent à TOUS les
 * types de services (pressing, réparation, soins, jardinage...).
 * Ils ne changent JAMAIS quand on ajoute un nouveau service.
 *
 * ─────────────────────────────────────────────────────────────────
 * PRINCIPE CLÉ du mémoire :
 *
 * ❌ AVANT (mauvaise architecture) :
 *    EN_COLLECTE, EN_VERIFICATION, EN_TRAITEMENT,
 *    TRAITEMENT_TERMINE, EN_LIVRAISON, VERIFICATION_FINALE
 *    → Ces statuts ne s'appliquaient QU'AU pressing !
 *    → Ajouter "soins" obligeait à modifier cet enum → violation Couche 1
 *
 * ✅ APRÈS (architecture du mémoire) :
 *    Les étapes MÉTIER (collecte, traitement, livraison, diagnostic...)
 *    sont dans ConfigurationService.etapes (Couche 2 — JSON configurable).
 *    Cet enum ne contient que les états UNIVERSELS du cycle de vie.
 *
 * ─────────────────────────────────────────────────────────────────
 * Cycle de vie d'une commande :
 *
 *   CREE ──→ CONFIRMEE ──→ EN_COURS ──→ CLOTUREE
 *              │                │
 *              ↓                ↓
 *           ANNULEE          LITIGE ──→ CLOTUREE
 *              ↓
 *           ANNULEE
 *
 * À l'intérieur de EN_COURS, les étapes métier sont tracées dans
 * Commande.etapeCourante (ex: "collecte", "traitement", "livraison").
 * Ces étapes viennent de ConfigurationService.etapes (Couche 2).
 * ─────────────────────────────────────────────────────────────────
 */
//public enum StatutCommande {

    /**
     * La commande vient d'être créée par le client.
     * En attente d'acceptation par le prestataire.
     */
    //CREE,

    /**
     * Le prestataire a accepté la commande.
     * Si exigeDevis=true : le devis a été envoyé et accepté.
     * Le workflow peut démarrer.
     */
    //CONFIRMEE,

    /**
     * Le workflow est en cours d'exécution.
     * L'étape précise est dans Commande.etapeCourante
     * (lue depuis ConfigurationService.etapes).
     *
     * pressing   → etapeCourante : "collecte" | "traitement" | "livraison"
     * reparation → etapeCourante : "diagnostic" | "reparation" | "restitution"
     * soins      → etapeCourante : "reservation" | "intervention" | "validation"
     */
    //EN_COURS,

    /**
     * La commande est terminée et validée.
     * Le client a confirmé la réception / la satisfaction.
     * Le paiement peut être finalisé.
     */
    //CLOTUREE,

    /**
     * La commande a été annulée (par le client ou le prestataire).
     * Raison enregistrée dans Commande.raisonAnnulation.
     */
    //ANNULEE,

    /**
     * Un litige a été ouvert.
     * Un Litige est créé dans la table litiges.
     * Le workflow de résolution commence.
     */
    //LITIGE
//}
