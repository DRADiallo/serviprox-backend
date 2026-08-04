package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.ConfigPrestataireDTO;
import com.serviprox.serviprox_backend.entity.ConfigPrestataire;
import com.serviprox.serviprox_backend.entity.ConfigurationService;
import com.serviprox.serviprox_backend.repository.ConfigPrestataireRepository;
import com.serviprox.serviprox_backend.repository.ConfigurationServiceRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigPrestataireService {

    private final ConfigPrestataireRepository configPrestataireRepository;
    private final PrestataireRepository prestataireRepository;
    private final ConfigurationServiceRepository configurationServiceRepository;

    // ═══ CREATE / UPDATE ═══
    // Créer ou mettre à jour la config d'un prestataire
    // Si config existe → mise à jour
    // Si première fois → création

    public ConfigPrestataireDTO configurerService(
            Long prestataireId,
            String typeService,
            String tarifJson,
            String optionJson,
            String prestationJson,
            String zoneCouverture){
        var prestataire = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));

        // Vérifier que le type de service existe
        // pressing, reparation, soins

        ConfigurationService configService= configurationServiceRepository
                .findByTypeService(typeService).orElseThrow(()-> new RuntimeException("Type de Service Inconnu : "+ typeService));

        // Chercher config existante ou créer nouvelle
        ConfigPrestataire configPrest= configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElse(new ConfigPrestataire());

        configPrest.setPrestataire(prestataire);
        configPrest.setConfigurationService(configService);
        configPrest.setTarifsPersonnalises(tarifJson);
        configPrest.setOptionsActives(optionJson);
        configPrest.setPrestationsAjoutees(prestationJson);
        configPrest.setZoneCouverture(zoneCouverture);

        return toDTO(configPrestataireRepository.save(configPrest));


    }
    // ═══ READ BY PRESTATAIRE ═══
    // Flutter lit cette config pour afficher :
    // - Les tarifs au client
    // - Les options disponibles
    // - La zone de couverture
    public ConfigPrestataireDTO getByPrestataire(Long prestaatireId){

        return toDTO(configPrestataireRepository
                .findByPrestataireId(prestaatireId)
                .orElseThrow(()-> new RuntimeException("Configuration introuvable --??"+ "Configurez d'abord votre service")));

    }

    // ═══ READ BY ID ═══
    public ConfigPrestataireDTO getById(Long id){

        return toDTO(configPrestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Configuration Introuvable"))
        );

    }

    // ═══ READ ALL ═══
    // Utilisé par AdminService pour supervision

    public List<ConfigPrestataireDTO> getAll(){

        return configPrestataireRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());


    }

    // ═══ UPDATE TARIFS UNIQUEMENT ═══
    // Augmentation des prix en période de fêtes
     public ConfigPrestataireDTO modifierTarifs(Long prestataireId, String tarifsJson){
        ConfigPrestataire configPrest= configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElseThrow(()-> new RuntimeException("Configuration Introuvable"));

        configPrest.setTarifsPersonnalises(tarifsJson);
        return toDTO(configPrestataireRepository.save(configPrest));

     }

    // ═══ UPDATE ZONE UNIQUEMENT ═══
    // Un pressing qui s'agrandit étend sa zone
    // Ex: "Médina" → "Médina, Plateau, Grand Dakar"
     public ConfigPrestataireDTO modifierZoneCouverture(Long prestataireId, String zoneCouv){

        ConfigPrestataire configPress= configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));

        configPress.setZoneCouverture(zoneCouv);
        return toDTO(configPrestataireRepository.save(configPress));

     }

    // ═══ UPDATE OPTIONS UNIQUEMENT ═══
    // Activer/désactiver des options
    // Ex: activer "optionExpress" pour les fêtes

    public ConfigPrestataireDTO modifierOptions(Long prestataireId, String optionJson){

        ConfigPrestataire configPress= configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));

        configPress.setOptionsActives(optionJson);
        return toDTO(configPrestataireRepository.save(configPress));

    }

    // ═══ DELETE ═══
    // Supprimer la configuration
    // Rare — généralement on reconfigure

    public void supprimerConfig(Long id){

        if (!configPrestataireRepository.existsById(id)){
            throw new RuntimeException("Configuration INtrouvable");

        }
        configPrestataireRepository.deleteById(id);
    }


    // Méthode utilitaire — crée service si absent
        private void creerSiAbsent( String typeService, String label,
    String etapes, boolean exigeDevis,  boolean exigePreuve ){

            if (configurationServiceRepository
                    .findByTypeService(typeService)
                    .isEmpty()){
                ConfigurationService cs= new ConfigurationService();
                cs.setTypeService(typeService);
                cs.setLabelAffiche(label);
                cs.setEtapes(etapes);
                cs.setExigeDevis(exigeDevis);
                cs.setExigePreuve(exigePreuve);

            }


    }

    // ═══ MAPPER entité → DTO ═══
    public ConfigPrestataireDTO toDTO(ConfigPrestataire cPrest){
         ConfigPrestataireDTO cdto= new ConfigPrestataireDTO();
         cdto.setId(cPrest.getId());
         cdto.setTarifsPersonnalises(cPrest.getTarifsPersonnalises());
         cdto.setOptionsActives(cPrest.getOptionsActives());
         cdto.setPrestationsAjoutees(cPrest.getPrestationsAjoutees());
         cdto.setZoneCouverture(cPrest.getZoneCouverture());
         if (cPrest.getPrestataire() !=null)
             cdto.setPrestataireId(cPrest.getPrestataire().getId());
         if (cPrest.getConfigurationService() !=null)
             cdto.setTypeService(cPrest.getConfigurationService().getTypeService());
         return cdto;



    }



}

