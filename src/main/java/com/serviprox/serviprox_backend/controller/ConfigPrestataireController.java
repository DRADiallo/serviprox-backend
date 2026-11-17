package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.ConfigPrestataireDTO;
import com.serviprox.serviprox_backend.dto.ConfigurationCollecteRequest;
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

    // ═══════════════════════════════════════════════════════════════
    // POST /api/prestataire/config/{prestataireId}
    // Prestataire configure son service (création ou mise à jour)
    // Ex : Pressing Médina configure ses tarifs, zone, étapes
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/{prestataireId}")
    public ResponseEntity<ConfigPrestataireDTO> configurer(
            @PathVariable Long prestataireId,
            @RequestParam String typeService,
            @RequestParam String tarifsJson,
            @RequestParam String optionJson,
            @RequestParam String prestationsJson,
            @RequestParam String zoneCouverture,
            @RequestParam(required = false) String etapesPersonnalisees,   // ← NOUVEAU (optionnel)
            @RequestParam(required = false) String modesDepotAcceptes,     // ← NOUVEAU (optionnel)
            @RequestParam(required = false) Boolean avecAgent) {           // ← NOUVEAU (optionnel)

        return ResponseEntity.ok(
                configPrestataireService.configurerService(
                        prestataireId,
                        typeService,
                        tarifsJson,
                        optionJson,
                        prestationsJson,
                        zoneCouverture,
                        etapesPersonnalisees,  // ← ajouté
                        modesDepotAcceptes,    // ← ajouté
                        avecAgent              // ← ajouté
                )
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/prestataire/config/{prestataireId}
    // Flutter lit la config du prestataire
    // → affiche tarifs, options, avecAgent au client
    // Retourne 404 si pas encore configuré (non 500)
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/{prestataireId}")
    public ResponseEntity<?> getByPrestataire(
            @PathVariable Long prestataireId) {
        try {
            return ResponseEntity.ok(
                    configPrestataireService.getByPrestataire(prestataireId));
        } catch (RuntimeException e) {
            // Pas encore configuré → 404 (Flutter affiche bannière "Configurez votre service")
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/prestataire/config
    // Admin — toutes les configurations
    // ═══════════════════════════════════════════════════════════════
    @GetMapping
    public ResponseEntity<List<ConfigPrestataireDTO>> getAll() {
        return ResponseEntity.ok(configPrestataireService.getAll());
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/tarifs
    // Modifier uniquement les tarifs
    // Cas fréquent : augmentation Tabaski / Korité
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/tarifs")
    public ResponseEntity<ConfigPrestataireDTO> modifierTarifs(
            @PathVariable Long prestataireId,
            @RequestParam String tarifsJson) {
        return ResponseEntity.ok(configPrestataireService.modifierTarifs(prestataireId, tarifsJson));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/zone
    // Modifier uniquement la zone
    // Ex : pressing qui s'agrandit vers un nouveau quartier
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/zone")
    public ResponseEntity<ConfigPrestataireDTO> modifierZone(
            @PathVariable Long prestataireId,
            @RequestParam String zone) {
        return ResponseEntity.ok(configPrestataireService.modifierZoneCouverture(prestataireId, zone));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/options
    // Modifier uniquement les options actives
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/options")
    public ResponseEntity<ConfigPrestataireDTO> modifierOptions(
            @PathVariable Long prestataireId,
            @RequestParam String optionJson) {
        return ResponseEntity.ok(configPrestataireService.modifierOptions(prestataireId, optionJson));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/etapes
    // Prestataire personnalise ses étapes de workflow
    // Ex : pressing ajoute "sechage" entre traitement et restitution
    // → ZÉRO ligne de code Java modifiée !
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/etapes")
    public ResponseEntity<ConfigPrestataireDTO> modifierEtapes(
            @PathVariable Long prestataireId,
            @RequestParam String etapesJson) {
        return ResponseEntity.ok(configPrestataireService.modifierEtapes(prestataireId, etapesJson));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/disponibilite
    // Activer / désactiver la disponibilité
    // Ex : pressing fermé pendant Tabaski
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/disponibilite")
    public ResponseEntity<ConfigPrestataireDTO> toggleDisponibilite(
            @PathVariable Long prestataireId,
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(configPrestataireService.toggleDisponibilite(prestataireId, disponible));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/prestataire/config/{prestataireId}/activer-collecte-livraison
    // Prestataire active (ou désactive) collecte & livraison depuis son profil
    // SANS modifier le code — le grand argument du mémoire !
    //
    // Body JSON :
    // {
    //   "avecAgent": true,
    //   "modesDepotAcceptes": "[\"Collecte domicile\",\"Dépôt atelier (livraison)\"]",
    //   "etapesPersonnalisees": "[\"collecte\",\"traitement\",\"livraison\"]"
    // }
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{prestataireId}/activer-collecte-livraison")
    public ResponseEntity<ConfigPrestataireDTO> activerCollecteLivraison(
            @PathVariable Long prestataireId,
            @RequestBody ConfigurationCollecteRequest req) {
        return ResponseEntity.ok(
                configPrestataireService.configurerCollecteLivraison(
                        prestataireId,
                        req.getAvecAgent(),
                        req.getModesDepotAcceptes(),
                        req.getEtapesPersonnalisees()
                )
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE /api/prestataire/config/{id}
    // Supprimer une configuration
    // ═══════════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        configPrestataireService.supprimerConfig(id);
        return ResponseEntity.ok().build();
    }
}