package com.serviprox.serviprox_backend.entity;

import lombok.Data;
/**
 * Représente un article dans un bon
 * de collecte ou de livraison
 *
 * Exemple pressing sénégalais :
 * type     : "boubou"
 * quantite : 2
 * couleur  : "bleu"
 * etat     : "BON"
 */
@Data
public class BonArticle {

    // Type d'article
    // Ex: chemise, boubou, bazin, dentelle
    // Vient de ConfigurationService → typesPrestation
    // → levier openEHR : admin ajoute "navetane"
    //   → Flutter affiche automatiquement
    private String type;

    // Quantité de cet article
    private Integer quantite;

    // Couleur principale
    // Crucial au Sénégal pour différencier
    // les boubous similaires
    private String couleur;

    // État de l'article
    // BON, TACHE, ABIME, MANQUANT
    private String etat;

    // URL photo de l'article
    // Prise par l'agent au moment de la collecte
    private String photoUrl;

    // ← MODE HYBRIDE
    // QR Code scanné si disponible
    // null si mode MANUEL
    private String qrCode;
}
