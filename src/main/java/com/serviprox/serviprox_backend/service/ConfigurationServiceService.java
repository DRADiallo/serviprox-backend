package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.ConfigurationServiceDTO;
import com.serviprox.serviprox_backend.entity.ConfigurationService;
import com.serviprox.serviprox_backend.repository.ConfigurationServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class  ConfigurationServiceService {
    private final ConfigurationServiceRepository configServiceRepo;

    // ═══ CREATE ═══
    // L'admin crée un nouveau type de service
    // depuis React SANS modifier le code
    //
    // Exemple concret :
    // Admin veut ajouter "Jardinage" :
    // typeService   : "jardinage"
    // labelAffiche  : "Service de Jardinage"
    // etapes        : ["devis","intervention","validation"]
    // exigeDevis    : true
    // exigePreuve   : true
    //
    // → Immédiatement disponible sur Flutter
    // → Aucune ligne de code modifiée !
    // C'est exactement le principe openEHR :
    // un nouvel archétype sans toucher
    // au Reference Model

    public ConfigurationService ceer(ConfigurationService configServ){
        // Vérifier que le typeService est unique
        if (configServiceRepo.findByTypeService(configServ.getTypeService()).isPresent()){
            throw new RuntimeException("un service "+ configServ.getTypeService() + "existe déjà ");

        }
        // Actif par défaut dès la création
        configServ.setActif(true);
        return configServiceRepo.save(configServ);

    }

    // ═══ READ ALL ACTIFS (Flutter) ═══
    // Seuls les services actifs sont visibles
    // aux clients dans l'application mobile
    public List<ConfigurationService>getAllActifs(){
        return  configServiceRepo.findByActifTrue();
    }

    // ═══ READ ALL (Admin React) ═══
    // L'admin voit TOUS les services
    // y compris les désactivés
    // pour pouvoir les réactiver si besoin

    public List<ConfigurationService>getAll(){
        return configServiceRepo.findAll();
    }

    // ═══ READ BY TYPE ═══

    public ConfigurationService getByType(String typeService){

        return configServiceRepo
                .findByTypeService(typeService)
                .orElseThrow(()-> new RuntimeException("Service introuvable "+ typeService));

    }

    // ═══ READ BY ID ═══
    public  ConfigurationService getById(Long id){

        return configServiceRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Configuratiomn intouvable"));

    }

    // ═══ UPDATE ═══
    // L'admin modifie un type de service existant
    // Ex: ajouter une étape au workflow jardinage
    // ou changer le label affiché
    // Sans toucher au code Spring Boot !

    public ConfigurationService modifier(Long id, ConfigurationService nouveau){

        ConfigurationService existing =configServiceRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Configuration Introuvable"));

        // On ne modifie que les champs fournis
        // Les autres restent inchangés

        if (nouveau.getLabelAffiche() !=null)
            existing.setLabelAffiche(nouveau.getLabelAffiche());
        if (nouveau.getEtapes() !=null)
            existing.setEtapes(nouveau.getEtapes());
        if (nouveau.getChampsCommande() !=null)
            existing.setChampsCommande(nouveau.getChampsCommande());
        if (nouveau.getTypesPrestation() !=null)
            existing.setTypesPrestation(nouveau.getTypesPrestation());
        if (nouveau.getOptionsDisponibles() !=null)
            existing.setOptionsDisponibles(nouveau.getOptionsDisponibles());
        if (nouveau.getExigeDevis() !=null)
            existing.setExigeDevis(nouveau.getExigeDevis());
        if (nouveau.getExigePreuve() !=null)
            existing.setExigePreuve(nouveau.getExigePreuve());

        return configServiceRepo.save(existing);

    }
    // ═══ ACTIVER ═══
    // Rendre un service visible sur Flutter
    public ConfigurationService activer(Long id){

        ConfigurationService cs= configServiceRepo
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Configuration Introuvable"));

        cs.setActif(true);
        return  configServiceRepo.save(cs);

    }

    // ═══ DÉSACTIVER ═══
    // On le désactive → invisible sur Flutter
    public ConfigurationService desactiver(Long id){

        ConfigurationService cs= configServiceRepo
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Configuration Introuvable"));

        cs.setActif(false);
        return  configServiceRepo.save(cs);

    }

    // ═══ INITIALISER PAR DÉFAUT ═══
    // Appelé au démarrage de l'application
    // Crée les 3 services de base si absents
    // L'admin peut en ajouter autant qu'il veut
    // via React sans modifier le code

    public void initialiserParDefaut(){

        creerSiAbsent(
                "pressing",
                "Service de Pressing",
                "[\"collecte\", \"traitement\",\"livraison\"]",
                // Champs spécifiques pressing
                "[{\"code\":\"typeArticle\"," + "" +
                        ""+"\"type\":\"LIST\","
                +"\"obligatoire\":true},"
                            +"{\"code\":\"quantite\","
                            +"\"type\":\"NUMBER\","
                            +"\"obligatoire\":true},"
                            +"\"code\":\"modeDepot\","
                            +"\"type\":\"LIST\","
                            +"\"obligatoire\":true}]",
                                false,
                                true

        );

        creerSiAbsent(
                "reparation",
                "Service de Réparation",
                "[\"diagnostic\",\"devis\","
                        + "\"reparation\",\"restitution\"]",
                // Champs spécifiques réparation
                "[{\"code\":\"typeEquipement\","
                        + "\"type\":\"LIST\","
                        + "\"obligatoire\":true},"
                        + "{\"code\":\"descriptionPanne\","
                        + "\"type\":\"TEXT\","
                        + "\"obligatoire\":true},"
                        + "{\"code\":\"photosAppareil\","
                        + "\"type\":\"FILE\","
                        + "\"obligatoire\":true}]",
                // Devis OBLIGATOIRE avant toute réparation
                true,
                // Preuve obligatoire (photo avant/après)
                true
        );



        creerSiAbsent(
                "soins",
                "Soins & Assistance à Domicile",
                "[\"reservation\",\"deplacement\","
                        + "\"intervention\",\"validation\"]",
                // Champs spécifiques soins
                "[{\"code\":\"typeSoin\","
                        + "\"type\":\"LIST\","
                        + "\"obligatoire\":true},"
                        + "{\"code\":\"dateIntervention\","
                        + "\"type\":\"DATE\","
                        + "\"obligatoire\":true},"
                        + "{\"code\":\"heureIntervention\","
                        + "\"type\":\"TIME\","
                        + "\"obligatoire\":true},"
                        + "{\"code\":\"dureeEstimee\","
                        + "\"type\":\"NUMBER\","
                        + "\"obligatoire\":true}]",
                // Pas de devis (tarif affiché avant)
                false,
                // Preuve obligatoire (photo + validation)
                true
        );
    }

    // ═══ MÉTHODE PRIVÉE UTILITAIRE ═══
    // Crée le service uniquement s'il n'existe pas
    // Évite les doublons au redémarrage
     private  void creerSiAbsent(
             String typeService,
             String  label,
             String etapes,
             String champsCommande,
             boolean exigeDevis,
             boolean exigePreuve
     ){
        if (configServiceRepo
                .findByTypeService(typeService)
                .isEmpty()){
            ConfigurationService cs = new ConfigurationService();
            cs.setTypeService(typeService);
            cs.setLabelAffiche(label);
            cs.setEtapes(etapes);
            cs.setChampsCommande(champsCommande);
            cs.setExigeDevis(exigeDevis);
            cs.setExigePreuve(exigePreuve);
            // actif par defaut
            cs.setActif(true);
            configServiceRepo.save(cs);
        }
     }

    // ═══ MAPPER entité → DTO ═══
    public ConfigurationServiceDTO toDTO(ConfigurationService cs){
        ConfigurationServiceDTO dto= new ConfigurationServiceDTO();
        dto.setId(cs.getId());
        dto.setTypeService(cs.getTypeService());
        dto.setLabelAffiche(cs.getLabelAffiche());
        dto.setEtapes(cs.getEtapes());
        dto.setChampsCommande(cs.getChampsCommande());
        dto.setTypesPrestation(cs.getTypesPrestation());
        dto.setOptionsDisponibles(cs.getOptionsDisponibles());
        dto.setExigeDevis(cs.getExigeDevis());
        dto.setExigePreuve(cs.getExigePreuve());
        dto.setActif(cs.getActif());
        return dto;

    }




}
