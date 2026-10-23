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

    // ═══════════════════════════════════════════════════════════════
    // CREATE / UPDATE — Prestataire configure son service
    // ═══════════════════════════════════════════════════════════════
    // Appelé lors de l'inscription ou de la mise à jour du profil.
    // Si config existe → mise à jour. Si nouvelle → création.
    //
    // Exemple pressing sans agent :
    //   typeService         = "pressing"
    //   avecAgent           = false
    //   etapesPersonnal.    = ["reception","traitement","restitution"]
    //   modesDepotAcceptes  = ["Dépôt et récupération"]
    public ConfigPrestataireDTO configurerService(
            Long prestataireId,
            String typeService,
            String tarifJson,
            String optionJson,
            String prestationJson,
            String zoneCouverture,
            String etapesPersonnalisees,   // ← NOUVEAU
            String modesDepotAcceptes,     // ← NOUVEAU
            Boolean avecAgent) {           // ← NOUVEAU

        var prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire introuvable"));

        // Vérifie que le type de service existe en base (Couche 2)
        ConfigurationService configService = configurationServiceRepository
                .findByTypeService(typeService)
                .orElseThrow(() -> new RuntimeException("Type de service inconnu : " + typeService));

        // Chercher config existante ou créer nouvelle
        ConfigPrestataire configPrest = configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElse(new ConfigPrestataire());

        configPrest.setPrestataire(prestataire);
        configPrest.setConfigurationService(configService);
        configPrest.setTarifsPersonnalises(tarifJson);
        configPrest.setOptionsActives(optionJson);
        configPrest.setPrestationsAjoutees(prestationJson);
        configPrest.setZoneCouverture(zoneCouverture);
        configPrest.setEtapesPersonnalisees(etapesPersonnalisees);  // ← NOUVEAU
        configPrest.setModesDepotAcceptes(modesDepotAcceptes);      // ← NOUVEAU
        configPrest.setAvecAgent(avecAgent);                        // ← NOUVEAU
        configPrest.setDisponible(true);

        return toDTO(configPrestataireRepository.save(configPrest));
    }

    // ═══════════════════════════════════════════════════════════════
    // READ
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO getByPrestataire(Long prestataireId) {
        return toDTO(configPrestataireRepository
                .findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException(
                        "Configuration introuvable — configurez d'abord votre service")));
    }

    public ConfigPrestataireDTO getById(Long id) {
        return toDTO(configPrestataireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable")));
    }

    public List<ConfigPrestataireDTO> getAll() {
        return configPrestataireRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE TARIFS
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO modifierTarifs(Long prestataireId, String tarifsJson) {
        ConfigPrestataire cp = configPrestataireRepository.findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cp.setTarifsPersonnalises(tarifsJson);
        return toDTO(configPrestataireRepository.save(cp));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE ZONE
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO modifierZoneCouverture(Long prestataireId, String zoneCouv) {
        ConfigPrestataire cp = configPrestataireRepository.findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cp.setZoneCouverture(zoneCouv);
        return toDTO(configPrestataireRepository.save(cp));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE OPTIONS
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO modifierOptions(Long prestataireId, String optionJson) {
        ConfigPrestataire cp = configPrestataireRepository.findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cp.setOptionsActives(optionJson);
        return toDTO(configPrestataireRepository.save(cp));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE ÉTAPES — LE PRESTATAIRE PERSONNALISE SON WORKFLOW
    // ═══════════════════════════════════════════════════════════════
    // Exemple : pressing sans agent ajoute "sechage"
    // ["reception","traitement","sechage","restitution"]
    // → Zéro ligne de code Java modifiée !
    public ConfigPrestataireDTO modifierEtapes(Long prestataireId, String etapesJson) {
        ConfigPrestataire cp = configPrestataireRepository.findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cp.setEtapesPersonnalisees(etapesJson);
        return toDTO(configPrestataireRepository.save(cp));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE DISPONIBILITÉ
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO toggleDisponibilite(Long prestataireId, Boolean disponible) {
        ConfigPrestataire cp = configPrestataireRepository.findByPrestataireId(prestataireId)
                .orElseThrow(() -> new RuntimeException("Configuration introuvable"));
        cp.setDisponible(disponible);
        return toDTO(configPrestataireRepository.save(cp));
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════════════════════════
    public void supprimerConfig(Long id) {
        if (!configPrestataireRepository.existsById(id)) {
            throw new RuntimeException("Configuration introuvable");
        }
        configPrestataireRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPER entité → DTO
    // ═══════════════════════════════════════════════════════════════
    public ConfigPrestataireDTO toDTO(ConfigPrestataire cp) {
        ConfigPrestataireDTO dto = new ConfigPrestataireDTO();
        dto.setId(cp.getId());
        dto.setTarifsPersonnalises(cp.getTarifsPersonnalises());
        dto.setOptionsActives(cp.getOptionsActives());
        dto.setPrestationsAjoutees(cp.getPrestationsAjoutees());
        dto.setZoneCouverture(cp.getZoneCouverture());
        dto.setEtapesPersonnalisees(cp.getEtapesPersonnalisees());  // ← NOUVEAU
        dto.setModesDepotAcceptes(cp.getModesDepotAcceptes());      // ← NOUVEAU
        dto.setAvecAgent(cp.getAvecAgent());                        // ← NOUVEAU
        dto.setDisponible(cp.getDisponible());                      // ← NOUVEAU

        if (cp.getPrestataire() != null) {
            dto.setPrestataireId(cp.getPrestataire().getId());
            dto.setNomPrestataire(cp.getPrestataire().getNomCommercial());
        }
        if (cp.getConfigurationService() != null) {
            dto.setTypeService(cp.getConfigurationService().getTypeService());
        }
        return dto;
    }
}