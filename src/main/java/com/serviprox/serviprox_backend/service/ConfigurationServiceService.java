package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.ConfigurationServiceDTO;
import com.serviprox.serviprox_backend.entity.ConfigurationService;
import com.serviprox.serviprox_backend.repository.ConfigurationServiceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceService {

    private final ConfigurationServiceRepository configServiceRepo;

    // ═══════════════════════════════════════════════════════════════
    // CREATE — L'admin crée un nouveau type de service via React
    // ═══════════════════════════════════════════════════════════════
    // Exemple : l'admin ajoute "jardinage" :
    //   typeService  : "jardinage"
    //   labelAffiche : "Service de Jardinage"
    //   etapes       : ["devis","intervention","validation"]
    //   exigeDevis   : true
    //
    // → Disponible dans Flutter IMMÉDIATEMENT
    // → Zéro ligne de code Java modifiée
    // → C'est le principe openEHR : nouvel archétype sans toucher
    //   au Modèle de Référence (Couche 1)
    public ConfigurationService creer(ConfigurationService configServ) {
        if (configServiceRepo.findByTypeService(configServ.getTypeService()).isPresent()) {
            throw new RuntimeException("Un service '" + configServ.getTypeService() + "' existe déjà.");
        }
        configServ.setActif(true);
        return configServiceRepo.save(configServ);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ ALL ACTIFS — Flutter (client mobile)
    // ═══════════════════════════════════════════════════════════════
    // Seuls les services actifs sont visibles dans l'application.
    public List<ConfigurationService> getAllActifs() {
        return configServiceRepo.findByActifTrue();
    }

    // ═══════════════════════════════════════════════════════════════
    // READ ALL — Admin React
    // ═══════════════════════════════════════════════════════════════
    // L'admin voit TOUS les services, y compris les désactivés.
    public List<ConfigurationService> getAll() {
        return configServiceRepo.findAll();
    }

    // ═══════════════════════════════════════════════════════════════
    // READ BY TYPE
    // ═══════════════════════════════════════════════════════════════
    public ConfigurationService getByType(String typeService) {
        return configServiceRepo
                .findByTypeService(typeService)
                .orElseThrow(() -> new RuntimeException("Service introuvable : " + typeService));
    }

    // ═══════════════════════════════════════════════════════════════
    // READ BY ID
    // ═══════════════════════════════════════════════════════════════
    public ConfigurationService getById(Long id) {
        return configServiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable : id=" + id));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE — L'admin modifie un service existant via React
    // ═══════════════════════════════════════════════════════════════
    // Mise à jour partielle : seuls les champs fournis sont modifiés.
    // Exemple : ajouter une étape au workflow pressing
    // ou modifier les valeurs du modeDepot → sans toucher au code !
    public ConfigurationService modifier(Long id, ConfigurationService nouveau) {
        ConfigurationService existing = configServiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable : id=" + id));

        if (nouveau.getLabelAffiche() != null)
            existing.setLabelAffiche(nouveau.getLabelAffiche());
        if (nouveau.getAvecAgent() != null)
            existing.setAvecAgent(nouveau.getAvecAgent());
        if (nouveau.getEtapes() != null)
            existing.setEtapes(nouveau.getEtapes());
        if (nouveau.getChampsCommande() != null)
            existing.setChampsCommande(nouveau.getChampsCommande());
        if (nouveau.getTypesPrestation() != null)
            existing.setTypesPrestation(nouveau.getTypesPrestation());
        if (nouveau.getExigeDevis() != null)
            existing.setExigeDevis(nouveau.getExigeDevis());
        if (nouveau.getExigePreuve() != null)
            existing.setExigePreuve(nouveau.getExigePreuve());

        return configServiceRepo.save(existing);
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTIVER / DÉSACTIVER
    // ═══════════════════════════════════════════════════════════════
    public ConfigurationService activer(Long id) {
        ConfigurationService cs = configServiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cs.setActif(true);
        return configServiceRepo.save(cs);
    }

    public ConfigurationService desactiver(Long id) {
        ConfigurationService cs = configServiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cs.setActif(false);
        return configServiceRepo.save(cs);
    }

    // ═══════════════════════════════════════════════════════════════
    // INITIALISER PAR DÉFAUT
    // ═══════════════════════════════════════════════════════════════
    // Appelé au démarrage de Spring Boot.
    // Crée les 3 services de base si absents en base.
    // L'admin peut en ajouter autant qu'il veut via React.
    //
    // Ces 3 configurations correspondent aux 3 cas d'usage
    // du Chapitre 2 du mémoire :
    //   Cas 1 → Pressing (avec agent collecteur/livreur)
    //   Cas 2 → Réparation petits équipements (sans agent)
    //   Cas 3 → Soins/assistance à domicile (sans agent)
    @PostConstruct
    public void initialiserParDefaut() {

        // ──────────────────────────────────────────────────────────
        // CAS 1 : PRESSING — avecAgent = TRUE
        // Workflow : collecte → traitement → livraison
        // modeDepot est un champ LIST dans champsCommande
        // (remplace l'ancien enum ModeDepot Java)
        // ──────────────────────────────────────────────────────────
        creerSiAbsent(
                "pressing",
                "Service de Pressing",
                true,     // avecAgent = true (agent collecte + livre)

                // typesPrestation : catégories de vêtements/articles
                "[{\"nom\":\"Lavage kilo\"},"
                        + "{\"nom\":\"Nettoyage sec\"},"
                        + "{\"nom\":\"Repassage\"},"
                        + "{\"nom\":\"Boubou / Bazin\"},"
                        + "{\"nom\":\"Darape\"},"
                        + "{\"nom\":\"Drap / Nappe\"},"
                        + "{\"nom\":\"Autre\"}]",

                // champsCommande : formulaire de commande DYNAMIQUE
                // Flutter construit ce formulaire depuis ce JSON.
                // modeDepot est ici → plus besoin d'enum Java !
                "[{\"code\":\"typeArticle\","
                        + "\"label\":\"Type d'article\","
                        + "\"type\":\"LIST\","
                        + "\"valeurs\":[\"Chemise\",\"Pantalon\",\"Robe\","
                        +              "\"Boubou / Bazin\",\"Darape\",\"Drap\",\"Autre\"],"
                        + "\"requis\":true},"
                        + "{\"code\":\"quantite\","
                        + "\"label\":\"Quantité\","
                        + "\"type\":\"NUMBER\","
                        + "\"requis\":true},"
                        // ← modeDepot remplace l'enum ModeDepot Java
                        + "{\"code\":\"modeDepot\","
                        + "\"label\":\"Mode de dépôt\","
                        + "\"type\":\"LIST\","
                        + "\"valeurs\":[\"Collecte domicile\","
                        +              "\"Dépôt atelier (livraison)\","
                        +              "\"Dépôt et récupération\"],"
                        + "\"requis\":true},"
                        + "{\"code\":\"descriptionColis\","
                        + "\"label\":\"Précisions (optionnel)\","
                        + "\"type\":\"TEXT\","
                        + "\"requis\":false}]",

                // etapes : workflow spécifique au pressing
                "[\"collecte\",\"traitement\",\"livraison\"]",

                false,  // exigeDevis : commande directe sans devis
                true    // exigePreuve : photos avant/après requises
        );

        // ──────────────────────────────────────────────────────────
        // CAS 2 : RÉPARATION — avecAgent = FALSE
        // Workflow : diagnostic → reparation → restitution
        // exigeDevis = true (le prestataire envoie un devis d'abord)
        // ──────────────────────────────────────────────────────────
        creerSiAbsent(
                "reparation",
                "Service de Réparation",
                false,    // avecAgent = false (client dépose directement)

                // typesPrestation : types d'appareils
                "[{\"nom\":\"Téléphone\"},"
                        + "{\"nom\":\"PC / Tablette\"},"
                        + "{\"nom\":\"Électroménager\"},"
                        + "{\"nom\":\"Télévision\"},"
                        + "{\"nom\":\"Autre\"}]",

                // champsCommande : formulaire de commande réparation
                "[{\"code\":\"typeAppareil\","
                        + "\"label\":\"Type d'appareil\","
                        + "\"type\":\"LIST\","
                        + "\"valeurs\":[\"Téléphone\",\"PC / Tablette\","
                        +              "\"Électroménager\",\"Télévision\",\"Autre\"],"
                        + "\"requis\":true},"
                        + "{\"code\":\"descriptionPanne\","
                        + "\"label\":\"Description de la panne\","
                        + "\"type\":\"TEXT\","
                        + "\"requis\":true},"
                        + "{\"code\":\"modePriseEnCharge\","
                        + "\"label\":\"Mode de prise en charge\","
                        + "\"type\":\"LIST\","
                        + "\"valeurs\":[\"Dépôt atelier\",\"Technicien à domicile\"],"
                        + "\"requis\":true}]",

                // etapes : workflow spécifique à la réparation
                "[\"diagnostic\",\"reparation\",\"restitution\"]",

                true,   // exigeDevis : devis obligatoire (réparation)
                true    // exigePreuve : photos de l'appareil requises
        );

        // ──────────────────────────────────────────────────────────
        // CAS 3 : SOINS À DOMICILE — avecAgent = FALSE
        // Workflow : reservation → intervention → validation
        // Rendez-vous avec date et heure
        // ──────────────────────────────────────────────────────────
        creerSiAbsent(
                "soins",
                "Soins à Domicile",
                false,    // avecAgent = false (prestataire se déplace)

                // typesPrestation : types de soins
                "[{\"nom\":\"Coiffure\"},"
                        + "{\"nom\":\"Beauté / Manucure\"},"
                        + "{\"nom\":\"Massage\"},"
                        + "{\"nom\":\"Soins bébé\"},"
                        + "{\"nom\":\"Nettoyage domicile\"},"
                        + "{\"nom\":\"Nettoyage bureau\"},"
                        + "{\"nom\":\"Désinfection\"},"
                        + "{\"nom\":\"Autre\"}]",

                // champsCommande : formulaire de commande soins
                "[{\"code\":\"typeSoin\","
                        + "\"label\":\"Type de soin\","
                        + "\"type\":\"LIST\","
                        + "\"valeurs\":[\"Coiffure\",\"Beauté / Manucure\","
                        +              "\"Massage\",\"Soins bébé\","
                        +              "\"Nettoyage\",\"Autre\"],"
                        + "\"requis\":true},"
                        + "{\"code\":\"dateRdv\","
                        + "\"label\":\"Date souhaitée\","
                        + "\"type\":\"DATE\","
                        + "\"requis\":true},"
                        + "{\"code\":\"heureRdv\","
                        + "\"label\":\"Heure souhaitée\","
                        + "\"type\":\"TIME\","
                        + "\"requis\":true},"
                        + "{\"code\":\"precision\","
                        + "\"label\":\"Précisions\","
                        + "\"type\":\"TEXT\","
                        + "\"requis\":false}]",

                // etapes : workflow spécifique aux soins
                "[\"reservation\",\"intervention\",\"validation\"]",

                false,  // exigeDevis : pas de devis pour soins simples
                true    // exigePreuve : photos après intervention
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODE PRIVÉE — crée le service uniquement s'il est absent
    // ═══════════════════════════════════════════════════════════════
    private void creerSiAbsent(
            String typeService,
            String labelAffiche,
            boolean avecAgent,
            String typesPrestation,
            String champsCommande,
            String etapes,
            boolean exigeDevis,
            boolean exigePreuve) {

        if (configServiceRepo.findByTypeService(typeService).isEmpty()) {
            ConfigurationService cs = new ConfigurationService();
            cs.setTypeService(typeService);
            cs.setLabelAffiche(labelAffiche);
            cs.setAvecAgent(avecAgent);
            cs.setTypesPrestation(typesPrestation);
            cs.setChampsCommande(champsCommande);
            cs.setEtapes(etapes);
            cs.setExigeDevis(exigeDevis);
            cs.setExigePreuve(exigePreuve);
            cs.setActif(true);
            configServiceRepo.save(cs);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPER entité → DTO
    // ═══════════════════════════════════════════════════════════════
    public ConfigurationServiceDTO toDTO(ConfigurationService cs) {
        ConfigurationServiceDTO dto = new ConfigurationServiceDTO();
        dto.setId(cs.getId());
        dto.setTypeService(cs.getTypeService());
        dto.setLabelAffiche(cs.getLabelAffiche());
        dto.setAvecAgent(cs.getAvecAgent());
        dto.setTypesPrestation(cs.getTypesPrestation());
        dto.setChampsCommande(cs.getChampsCommande());
        dto.setEtapes(cs.getEtapes());
        dto.setExigeDevis(cs.getExigeDevis());
        dto.setExigePreuve(cs.getExigePreuve());
        dto.setActif(cs.getActif());
        return dto;
    }
}