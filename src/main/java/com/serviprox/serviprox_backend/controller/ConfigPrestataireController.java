package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.dto.ConfigPrestataireDTO;
import com.serviprox.serviprox_backend.service.ConfigPrestataireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// URL : /api/prestataire/config
// Innovation openEHR — chaque prestataire
// configure son service sans modifier le code
@RestController
@RequestMapping("/api/prestataire/config")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConfigPrestataireController {

    private final ConfigPrestataireService configPrestataireService;


    // POST /api/prestataire/config/{prestataireId}
    // Prestataire configure son service
    // Ex: Pressing Médina configure ses tarifs
    //     et sa zone de couverture

    @PostMapping("/{prestataireId}")
    public ResponseEntity<ConfigPrestataireDTO> configurer(
            @PathVariable Long prestataireId,
            @RequestParam String typeService,
            @RequestParam String tarifsJson,
            @RequestParam String optionJson,
            @RequestParam String prestationsJson,
            @RequestParam String zoneCouverture){

        return ResponseEntity.ok(configPrestataireService.configurerService
                (prestataireId,typeService,tarifsJson,optionJson,prestationsJson,zoneCouverture));
    }

    // GET /api/prestataire/config/{prestataireId}
    // Flutter lit la config du prestataire
    // → affiche tarifs et options au client
    @GetMapping("/{prestataireId}")
    public ResponseEntity<ConfigPrestataireDTO> getByPrestataire(@PathVariable Long prestatareId){

        return ResponseEntity.ok(configPrestataireService.getByPrestataire(prestatareId));
    }

    // GET /api/prestataire/config
    // Admin — toutes les configurations
    @GetMapping
    public ResponseEntity<List<ConfigPrestataireDTO>>getAll(){

        return ResponseEntity.ok(configPrestataireService.getAll());
    }

    // PUT /api/prestataire/config/{id}/tarifs
    // Modifier uniquement les tarifs
    // Cas fréquent : augmentation Tabaski/Korité
    @PutMapping("/{prestataireId}/tarifs")
    public ResponseEntity<ConfigPrestataireDTO>modifierTarifs(@PathVariable Long prestataireId,@RequestParam String tarifsJson){

        return ResponseEntity.ok(configPrestataireService.modifierTarifs(prestataireId,tarifsJson));
    }

    // PUT /api/prestataire/config/{id}/zone
    // Modifier uniquement la zone
    // Pressing qui s'agrandit
    @PutMapping("/{prestataireId}/zone")
    public ResponseEntity<ConfigPrestataireDTO>modifierZone(@PathVariable Long prestataireId, @RequestParam String zone){

        return ResponseEntity.ok(configPrestataireService.modifierZoneCouverture(prestataireId,zone));
    }

    // PUT /api/prestataire/config/{id}/options
    // Modifier uniquement les options
    @PutMapping("/{prestataireId}/options")
    public ResponseEntity<ConfigPrestataireDTO>modifierOptions(@PathVariable Long prestataireId, @RequestParam String optionJson){

        return ResponseEntity.ok(configPrestataireService.modifierOptions(prestataireId,optionJson));
    }

    // DELETE /api/prestataire/config/{id}
    // Supprimer une configuration
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id){

        configPrestataireService.supprimerConfig(id);
        return ResponseEntity.ok().build();
    }


}
