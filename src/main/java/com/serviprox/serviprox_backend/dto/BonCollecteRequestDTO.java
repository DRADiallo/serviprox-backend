package com.serviprox.serviprox_backend.dto;
import com.serviprox.serviprox_backend.entity.BonArticle;
import lombok.Data;
import java.util.List;


/**
 * Requête envoyée par Flutter
 * lors de la collecte ou livraison
 *
 * Supporte deux modes :
 * MANUEL   → compteurs visuels Option D (maintenant)
 * QR_CODE  → scan étiquettes Option B (Chapitre 8)
 */
@Data
public class BonCollecteRequestDTO {

        // Commande concernée
        private Long commandeId;

        // Type de bon
        // "COLLECTE" ou "LIVRAISON"
        private String typeBon;

        // Mode de saisie choisi par l'agent
        // "MANUEL"   → compteurs visuels Flutter
        // "QR_CODE"  → scan étiquettes (Chapitre 8)
        private String modeSaisie;

        // Liste des articles
        // Remplie dans les deux modes
        private List<BonArticle> articles;

        // QR Codes scannés
        // Utilisé uniquement en mode QR_CODE
        // null si mode MANUEL
        private List<String> qrCodesScannees;

        // Signature numérique du client
        // URL de l'image de signature
        // Générée par Flutter (signature pad)
        private String signatureClientUrl;

        // Géolocalisation de l'agent
        // Format : "14.7167,-17.4677" (lat,lng Dakar)
        // Horodatage automatique côté serveur
        private String geoLocalisation;

        // ID de l'agent qui effectue la collecte
        private Long agentId;

}
