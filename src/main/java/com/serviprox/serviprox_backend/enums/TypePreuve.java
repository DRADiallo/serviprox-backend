package com.serviprox.serviprox_backend.enums;

public enum TypePreuve {
    PHOTO,
    VIDEO,
    SIGNATURE,

    // ← AJOUT : types spécifiques
    // Pour tous les services
    AVANT_INTERVENTION,    // Photo état initial
    APRES_INTERVENTION,    // Photo résultat final

    // Pour Pressing / Livraison
    COLLECTE_CLIENT,       // Agent chez client
    RECEPTION_PRESTATAIRE, // Prestataire reçoit
    LIVRAISON_CLIENT,      // Agent livre

    // Paiement
    PREUVE_PAIEMENT        // Capture Wave/Orange

}
