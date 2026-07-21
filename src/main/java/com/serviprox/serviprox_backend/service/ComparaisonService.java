package com.serviprox.serviprox_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviprox.serviprox_backend.entity.BonArticle;
import com.serviprox.serviprox_backend.entity.ComparaisonResultat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparaisonService {

    private final ObjectMapper objectMapper;

    // ═══ MÉTHODE PRINCIPALE ═══
    // Choisit automatiquement la meilleure
    // méthode disponible
    // 1. Essaie Google Vision API
    // 2. Si indisponible → fallback JSON
    public ComparaisonResultat comparer( String jsonBonCollecte, String jsonBonLivraison) {

        try {
            // Tenter Google Vision d'abord
            return comparerAvecGoogleVision(jsonBonCollecte, jsonBonLivraison);

        } catch (Exception e) {
            // Google Vision indisponible
            // → fallback comparaison JSON
            return comparerParMetadonnees(jsonBonCollecte, jsonBonLivraison);
        }
    }

    // ═══ OPTION D — Comparaison JSON ═══
    // Opérationnelle maintenant
    // Fonctionne hors ligne
    // Compare article par article
    // Adapté au contexte sénégalais
    public ComparaisonResultat comparerParMetadonnees(String jsonBonCollecte, String jsonBonLivraison) {

        ComparaisonResultat resultat = new ComparaisonResultat();

        resultat.setMethodeUtilisee("JSON_METADATA");

        List<String> ecarts = new ArrayList<>();

        try {
            // Parser les deux JSON

            List<BonArticle> articlesCollectes = objectMapper.readValue( jsonBonCollecte,
                    new TypeReference<List<BonArticle>>() {});


            List<BonArticle> articlesLivres = objectMapper.readValue( jsonBonLivraison,
                    new TypeReference<List<BonArticle>>() {});


            // Grouper par type
            // Ex: {"boubou": [art1, art2]}
            Map<String, List<BonArticle>>
                    mapCollecte = articlesCollectes
                    .stream()
                    .collect(Collectors.groupingBy(
                            BonArticle::getType));

            Map<String, List<BonArticle>>
                    mapLivre = articlesLivres
                    .stream()
                    .collect(Collectors.groupingBy(
                            BonArticle::getType));

            // ── Vérification 1 : manquants ──
            // Présent à la collecte
            // mais absent à la livraison
            for (String type :
                    mapCollecte.keySet()) {
                if (!mapLivre.containsKey(type)) {
                    ecarts.add("❌ " + type
                            + " — ABSENT à la livraison");
                    continue;
                }

                // ── Vérification 2 : quantités ──
                int qteCollecte = mapCollecte
                        .get(type).stream()
                        .mapToInt(BonArticle::getQuantite)
                        .sum();

                int qteLivre = mapLivre
                        .get(type).stream()
                        .mapToInt(BonArticle::getQuantite)
                        .sum();

                if (qteCollecte != qteLivre) {
                    ecarts.add(  type + " — Collecté: " + qteCollecte + " / Livré: " + qteLivre);
                }

                // ── Vérification 3 : état ──
                // Article abîmé pendant traitement
                for (BonArticle aLivre : mapLivre.getOrDefault(type, List.of())) {

                    if ("ABIME".equals(aLivre.getEtat()) || "TACHE".equals(aLivre.getEtat())) {

                        ecarts.add("🔴 " + type + " (" + aLivre.getCouleur() + ") — État: " + aLivre.getEtat());
                    }
                }
            }

            // ── Vérification 4 : ajoutés ──
            // Présent à la livraison
            // mais absent à la collecte
            for (String type : mapLivre.keySet()) {
                if (!mapCollecte.containsKey(type)) {
                    ecarts.add("➕ " + type
                            + " — NON collecté "
                            + "(erreur de livraison)");
                }
            }

            // Calcul score de similarité
            // 100 = identiques
            // -20 points par écart détecté
            int totalArticles = articlesCollectes.size() + articlesLivres.size();

            double score = totalArticles > 0 ? Math.max(0, 100.0 - (ecarts.size() * 20.0)) : 100.0;

            resultat.setScoreSimiiliarite(score);
            resultat.setEcarts(ecarts);
            resultat.setIdentiques(ecarts.isEmpty());

            // Messages selon résultat
            if (ecarts.isEmpty()) {

                resultat.setMessageClient("Tous vos articles ont été " + "livrés correctement !");

                resultat.setMessagePrestataire(" Livraison conforme " + "au bon de collecte.");

            } else {
                // ← Correction : String propre
                // pas de conversion int → String
                resultat.setMessageClient( ecarts.size() + " écart(s) détecté(s) " + "dans votre livraison. " + "Un litige a été ouvert.");

                resultat.setMessagePrestataire( ecarts.size() + " écart(s) détecté(s) — " + "Vérifiez immédiatement :\n"
                                + String.join("\n", ecarts));
            }

        } catch (Exception e) {
            // Parsing JSON échoué
            // → comparaison basique
            resultat.setIdentiques(jsonBonCollecte.equals(jsonBonLivraison));

            resultat.setScoreSimiiliarite(resultat.isIdentiques() ? 100.0 : 0.0);

            resultat.setEcarts(List.of("Impossible d'analyser les bons"));
        }

        return resultat;
    }

    // ═══ OPTION A — Google Vision API ═══
    // Perspective Chapitre 8
    // Analyse IA des photos
    // Plus précis que comparaison JSON
    public ComparaisonResultat comparerAvecGoogleVision(String urlPhotoCollecte, String urlPhotoLivraison) {

        // TODO — Chapitre 8 : Perspective IA
        //
        // Implémentation prévue :
        // 1. Envoyer photo collecte à Vision API
        //    → Liste objets détectés
        //    → ["chemise x3", "boubou bleu x1"]
        //
        // 2. Envoyer photo livraison à Vision API
        //    → Liste objets détectés
        //
        // 3. Comparer les deux listes
        //    → Score similarité
        //    → Écarts automatiques
        //
        // HttpClient client =
        //     HttpClient.newHttpClient();
        // HttpRequest request =
        //     HttpRequest.newBuilder()
        //     .uri(URI.create(GOOGLE_VISION_URL))
        //     .header("Authorization",
        //             "Bearer " + API_KEY)
        //     .build();

        // Déclenche fallback JSON
        throw new UnsupportedOperationException("Google Vision API — " + "Perspective Chapitre 8");
    }

    // ═══ MÉTHODE UTILITAIRE ═══
    // Convertir liste articles → JSON
    // pour stockage en base
    public String genererJsonBon(List<BonArticle> articles) {

        try {
            return objectMapper.writeValueAsString(articles);
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération JSON bon");
        }
    }
}