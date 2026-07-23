package com.serviprox.serviprox_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviprox.serviprox_backend.dto.BonCollecteRequestDTO;
import com.serviprox.serviprox_backend.entity.BonArticle;
import com.serviprox.serviprox_backend.entity.BonCollectRequest;
import com.serviprox.serviprox_backend.entity.ComparaisonResultat;
import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PreuveRealisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonService {

    private final CommandeRepository
            commandeRepository;
    private final PreuveRealisationRepository
            preuveRepository;
    private final ComparaisonService
            comparaisonService;
    private final NotificationService
            notificationService;
    private final ObjectMapper objectMapper;

    // ═══ GÉNÉRER UN BON ═══
    // Supporte deux modes :
    // MANUEL   → Option D (prototype actuel)
    //            Compteurs visuels Flutter
    // QR_CODE  → Option B (Chapitre 8)
    //            Scan étiquettes pressing pro
    public PreuveRealisation genererBon(BonCollectRequest request) {

        var commande = commandeRepository
                .findById(request.getCommandeId())
                .orElseThrow(() ->
                        new RuntimeException("Commande introuvable"));

        List<BonArticle> articles;

        if ("QR_CODE".equals(
                request.getModeSaisie())) {
            // ── Mode QR Code ──
            // Perspective Chapitre 8
            articles = traiterQRCodes(
                    request.getQrCodesScannees());
        } else {
            // ── Mode Manuel ──
            // Option D — prototype actuel
            // Articles saisis via Flutter
            articles = request.getArticles();
        }

        // Convertir articles en JSON
        String articlesJson;
        try {
            articlesJson = objectMapper
                    .writeValueAsString(articles);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur conversion JSON articles");
        }

        // Créer la preuve de réalisation
        PreuveRealisation bon =
                new PreuveRealisation();
        bon.setCommande(commande);
        bon.setType(TypePreuve.PHOTO);
        // JSON articles + signature + géoloc
        bon.setContenu(articlesJson);
        bon.setDate(LocalDateTime.now());

        PreuveRealisation saved =
                preuveRepository.save(bon);

        // Notifier le client
        notificationService.envoyer(
                commande.getClient().getId(),
                "BON_GENERE",
                "📋 Bon de "
                        + request.getTypeBon().toLowerCase()
                        + " généré — Commande #"
                        + request.getCommandeId()
                        + " [Mode: " + request.getModeSaisie()
                        + "]"
        );

        return saved;
    }

    // ═══ COMPARER BONS ═══
    // Collecte vs Livraison
    // Détecte automatiquement les écarts
    public ComparaisonResultat comparerBons(
            Long commandeId,
            String jsonBonCollecte,
            String jsonBonLivraison) {

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        // Comparaison intelligente
        // JSON maintenant → Vision API Chapitre 8
        ComparaisonResultat resultat =
                comparaisonService.comparer(
                        jsonBonCollecte,
                        jsonBonLivraison);

        if (!resultat.isIdentiques()) {
            // Écarts → notifications litige
            notificationService.envoyer(
                    commande.getPrestataire().getId(),
                    "ECART_DETECTE",
                    resultat.getMessagePrestataire()
            );
            notificationService.envoyer(
                    commande.getClient().getId(),
                    "ECART_DETECTE",
                    resultat.getMessageClient()
            );
        } else {
            // Conforme → notification positive
            notificationService.envoyer(
                    commande.getClient().getId(),
                    "LIVRAISON_CONFORME",
                    resultat.getMessageClient()
            );
        }

        return resultat;
    }

    // ═══ QR CODE — Chapitre 8 ═══
    // Décoder les QR Codes scannés
    // en liste d'articles
    private List<BonArticle> traiterQRCodes(
            List<String> qrCodes) {

        // TODO Chapitre 8
        // Format QR Code prévu :
        // "PRESSING_42_BOUBOU_BLEU_BON"
        //  └─ prestataireId
        //      └─ type
        //          └─ couleur
        //              └─ état
        //
        // for (String qr : qrCodes) {
        //   String[] parts = qr.split("_");
        //   BonArticle art = new BonArticle();
        //   art.setType(parts[2]);
        //   art.setCouleur(parts[3]);
        //   art.setEtat(parts[4]);
        //   art.setQuantite(1);
        //   articles.add(art);
        // }

        throw new UnsupportedOperationException(
                "Mode QR Code — Perspective Chapitre 8"
                        + " — Utilisez le mode MANUEL"
        );
    }
}