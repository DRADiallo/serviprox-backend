package com.serviprox.serviprox_backend.enums;

public enum StatutCommande {
    CREE,
    CONFIRMEE,
    EN_COLLECTE,          // ← NOUVEAU
    EN_VERIFICATION,
    EN_TRAITEMENT,
    TRAITEMENT_TERMINE,   // ← Paiement intégré
    EN_LIVRAISON,
    VERIFICATION_FINALE,
    CLOTUREE,
    ANNULEE,
    LITIGE
}
