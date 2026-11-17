package com.serviprox.serviprox_backend.config;

import com.serviprox.serviprox_backend.entity.ConfigurationService;
import com.serviprox.serviprox_backend.repository.ConfigurationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════
 * DATA SEEDER — Alimentation initiale de la table configurations_service
 * ═══════════════════════════════════════════════════════════════════
 *
 * Principe du mémoire (Couche 2 — Métamodèle) :
 *   "Ajouter un service = créer 1 ligne en base.
 *    Zéro modification de code Java ou Flutter."
 *
 * Ce seeder insère les 3 types de services de base si et seulement si
 * ils n'existent pas encore (idempotent — sûr à redémarrer N fois).
 *
 * ───────────────────────────────────────────────────────────────────
 * Services seedés :
 *   1. pressing   — collecte/livraison, avec agents
 *   2. reparation — sur rendez-vous, nécessite devis
 *   3. soins      — sur rendez-vous, beauté & bien-être
 * ───────────────────────────────────────────────────────────────────
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final ConfigurationServiceRepository configurationServiceRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("═══ DataSeeder : vérification des types de service ═══");
        seedPressing();
        seedReparation();
        seedSoins();
        log.info("═══ DataSeeder : terminé ═══");
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. PRESSING
    // ─────────────────────────────────────────────────────────────────
    private void seedPressing() {
        if (configurationServiceRepository.findByTypeService("pressing").isPresent()) {
            log.info("  [OK] pressing — déjà en base, skip");
            return;
        }

        ConfigurationService pressing = new ConfigurationService();
        pressing.setTypeService("pressing");
        pressing.setLabelAffiche("Service de Pressing");
        pressing.setAvecAgent(true);   // agent collecte & livre à domicile

        // Étapes métier (jalons à l'intérieur de EN_COURS)
        pressing.setEtapes(
                "[\"collecte\",\"traitement\",\"livraison\"]"
        );

        // Formulaire de commande dynamique
        pressing.setChampsCommande(
                "[" +
                        "{\"code\":\"typeArticle\",\"label\":\"Type d'article\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"Chemise\",\"Pantalon\",\"Robe\",\"Boubou/Bazin\",\"Darape\",\"Djellaba\",\"Veste\",\"Linge de maison\"]," +
                        "\"requis\":true}," +
                        "{\"code\":\"quantite\",\"label\":\"Quantité\",\"type\":\"NUMBER\",\"requis\":true}," +
                        "{\"code\":\"modeDepot\",\"label\":\"Mode de dépôt\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"Collecte domicile\",\"Dépôt atelier (livraison)\",\"Dépôt et récupération\"]," +
                        "\"requis\":true}," +
                        "{\"code\":\"descriptionColis\",\"label\":\"Précisions supplémentaires\",\"type\":\"TEXT\",\"requis\":false}" +
                        "]"
        );

        // Catégories de prestations
        pressing.setTypesPrestation(
                "[" +
                        "{\"nom\":\"Lavage au kilo\"}," +
                        "{\"nom\":\"Nettoyage à sec\"}," +
                        "{\"nom\":\"Repassage uniquement\"}," +
                        "{\"nom\":\"Lavage + repassage\"}," +
                        "{\"nom\":\"Linge de maison\"}" +
                        "]"
        );

        pressing.setExigeDevis(false);   // tarif au kilo → pas de devis
        pressing.setExigePreuve(true);   // photos avant/après requises
        pressing.setActif(true);

        configurationServiceRepository.save(pressing);
        log.info("  [CRÉÉ] pressing");
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. REPARATION
    // ─────────────────────────────────────────────────────────────────
    private void seedReparation() {
        if (configurationServiceRepository.findByTypeService("reparation").isPresent()) {
            log.info("  [OK] reparation — déjà en base, skip");
            return;
        }

        ConfigurationService reparation = new ConfigurationService();
        reparation.setTypeService("reparation");
        reparation.setLabelAffiche("Réparation Équipements");
        reparation.setAvecAgent(false);  // pas d'agent : client apporte / technicien se déplace

        reparation.setEtapes(
                "[\"diagnostic\",\"reparation\",\"restitution\"]"
        );

        reparation.setChampsCommande(
                "[" +
                        "{\"code\":\"categorieAppareil\",\"label\":\"Catégorie d'appareil\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"Téléphone\",\"PC / Tablette\",\"Téléviseur\",\"Électroménager\",\"Autre\"]," +
                        "\"requis\":true}," +
                        "{\"code\":\"marqueModele\",\"label\":\"Marque / Modèle\",\"type\":\"TEXT\",\"requis\":true}," +
                        "{\"code\":\"descriptionPanne\",\"label\":\"Description de la panne\",\"type\":\"TEXT\",\"requis\":true}," +
                        "{\"code\":\"urgence\",\"label\":\"Niveau d'urgence\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"Normal\",\"Urgent\"],\"requis\":false}" +
                        "]"
        );

        reparation.setTypesPrestation(
                "[" +
                        "{\"nom\":\"Réparation téléphone\"}," +
                        "{\"nom\":\"Réparation PC / Tablette\"}," +
                        "{\"nom\":\"Réparation TV / Écran\"}," +
                        "{\"nom\":\"Réparation électroménager\"}," +
                        "{\"nom\":\"Diagnostic\"}," +
                        "{\"nom\":\"Autre réparation\"}" +
                        "]"
        );

        reparation.setExigeDevis(true);   // devis obligatoire avant confirmation
        reparation.setExigePreuve(true);  // photo de l'appareil réparé requise
        reparation.setActif(true);

        configurationServiceRepository.save(reparation);
        log.info("  [CRÉÉ] reparation");
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. SOINS
    // ─────────────────────────────────────────────────────────────────
    private void seedSoins() {
        if (configurationServiceRepository.findByTypeService("soins").isPresent()) {
            log.info("  [OK] soins — déjà en base, skip");
            return;
        }

        ConfigurationService soins = new ConfigurationService();
        soins.setTypeService("soins");
        soins.setLabelAffiche("Soins & Beauté");
        soins.setAvecAgent(false);   // prestataire reçoit en salon ou se déplace

        soins.setEtapes(
                "[\"reservation\",\"intervention\",\"validation\"]"
        );

        soins.setChampsCommande(
                "[" +
                        "{\"code\":\"typePrestation\",\"label\":\"Type de soin\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"Coiffure\",\"Tresse / Natte\",\"Massage\",\"Manucure / Pédicure\",\"Maquillage\",\"Soin du visage\",\"Autre\"]," +
                        "\"requis\":true}," +
                        "{\"code\":\"dateRdv\",\"label\":\"Date souhaitée\",\"type\":\"DATE\",\"requis\":true}," +
                        "{\"code\":\"heureRdv\",\"label\":\"Heure souhaitée\",\"type\":\"TIME\",\"requis\":true}," +
                        "{\"code\":\"lieuIntervention\",\"label\":\"Lieu\",\"type\":\"LIST\"," +
                        "\"valeurs\":[\"À domicile\",\"Au salon\"],\"requis\":true}," +
                        "{\"code\":\"precisionsComplementaires\",\"label\":\"Précisions\",\"type\":\"TEXT\",\"requis\":false}" +
                        "]"
        );

        soins.setTypesPrestation(
                "[" +
                        "{\"nom\":\"Coiffure\"}," +
                        "{\"nom\":\"Tressage / Nattage\"}," +
                        "{\"nom\":\"Massage\"}," +
                        "{\"nom\":\"Manucure / Pédicure\"}," +
                        "{\"nom\":\"Maquillage\"}," +
                        "{\"nom\":\"Soin du visage\"}" +
                        "]"
        );

        soins.setExigeDevis(false);    // tarifs fixes affichés
        soins.setExigePreuve(false);   // pas de photo requise
        soins.setActif(true);

        configurationServiceRepository.save(soins);
        log.info("  [CRÉÉ] soins");
    }
}
