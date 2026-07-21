package com.serviprox.serviprox_backend.entity;

import lombok.Data;

import java.util.List;

/**
 * Requête envoyée par Flutter
 * lors de la collecte ou livraison
 *
 * Supporte deux modes :
 * MANUEL   → compteurs visuels Option D
 * QR_CODE  → scan étiquettes Option B
 */
@Data
public class BonCollecteRequest {

    // Commande concernée
    private Long commandeId;

    // Type de bon
    // "COLLECTE" ou "LIVRAISON"
    private String typeBon;

    // ← MODE HYBRIDE
    // "MANUEL"   → agent saisit manuellement
    //              compteurs visuels Flutter
    // "QR_CODE"  → agent scanne les étiquettes
    //              pressing professionnel
    private String modeSaisie;

    // Liste des articles
    // Remplie dans les deux modes
    private List<BonArticle> articles;

    // QR Codes scannés (mode QR_CODE uniquement)
    // null si mode MANUEL
    private List<String> qrCodesScannees;

    // Signature numérique du client
    // URL de l'image de signature
    private String signatureClientUrl;

    // Géolocalisation de l'agent
    // Format : "14.7167,-17.4677"
    // Horodatage automatique côté serveur
    private String geoLocalisation;

    // ID de l'agent qui effectue la collecte
    private Long agentId;


}
