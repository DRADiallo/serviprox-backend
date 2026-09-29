package com.serviprox.serviprox_backend.enums;

public enum StatutCommande {
    CREE,
    CONFIRMEE,
    // AJOUT : vérification articles
    // Agent collecte + Prestataire reçoit
    // Les 3 acteurs vérifient
    EN_VERIFICATION,

    EN_TRAITEMENT,
    //  AJOUT : traitement terminé
    // En attente paiement client
    TRAITEMENT_TERMINE,

    // AJOUT : paiement reçu
    // Client paie + Prestataire confirme
    PAIEMENT_RECU,
    EN_LIVRAISON,
    VERIFICATION_FINALE,
    TERMINEE,
    CLOTUREE,
    ANNULEE,
    LITIGE
}
