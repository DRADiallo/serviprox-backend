package com.serviprox.serviprox_backend.entity;

import lombok.Data;

import java.util.List;

@Data
public class ComparaisonResultat {

    // true  → bons identiques → commande clôturée
    // false → écart détecté → litige automatique
    private boolean identiques;

    // Score de similarité entre 0 et 100
    // 100 = parfaitement identiques
    // < 80 = litige automatique
    private double scoreSimiiliarite;

    // Liste des écarts détectés
    // Ex: ["boubou bleu manquant",
    //      "chemise blanche tachée"]
    private List<String> ecarts;

    // Méthode utilisée pour la comparaison
    // "JSON_METADATA" ou "GOOGLE_VISION_API"
    private String methodeUtilisee;

    // Message explicatif pour le client
    private String messageClient;

    // Message pour le prestataire
    private String messagePrestataire;

}
