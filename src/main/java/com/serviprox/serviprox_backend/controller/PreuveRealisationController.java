package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.PreuveRealisationDTO;
import com.serviprox.serviprox_backend.service.PreuveRealisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.List.*;
import java.util.Map;


@RestController
@RequestMapping("/api/preuve")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PreuveRealisationController {

    private final PreuveRealisationService
            preuveRealisationService;

    // POST /api/preuve/commande/{commandeId}
    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<PreuveRealisationDTO> ajouterPreuve(@PathVariable Long commandeId, @RequestParam String typePreuve,
            @RequestParam Long acteurId, @RequestParam(required = false, defaultValue = "OK")
            String statut, @RequestParam(required = false) String commentaire, @RequestBody(required = false) Map<String, String> body) {

        String photoBase64 = body != null
                ? body.get("photoBase64") : null;

        return ResponseEntity.ok(
                preuveRealisationService.ajouterPreuve(
                        commandeId, typePreuve,
                        photoBase64, acteurId,
                        statut, commentaire));
    }

    // GET /api/preuve/commande/{commandeId}
    // ← CHANGEMENT : retourne List
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity <List<PreuveRealisationDTO>>getByCommande(@PathVariable Long commandeId) {

        return ResponseEntity.ok(preuveRealisationService.getByCommande(commandeId));
    }

    // GET /api/preuve/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PreuveRealisationDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(
                preuveRealisationService.getById(id));
    }

    // GET /api/preuve
    @GetMapping
    public ResponseEntity <List<PreuveRealisationDTO>> getAll() {return ResponseEntity.ok(preuveRealisationService.getAll());
    }

    // POST /api/preuve/comparer/{commandeId}
    @PostMapping("/comparer/{commandeId}")
    public ResponseEntity<Boolean> comparerBons(@PathVariable Long commandeId, @RequestParam String urlBonCollecte, @RequestParam String urlBonLivraison) {

        return ResponseEntity.ok(preuveRealisationService.comparerBons(
                        commandeId,
                        urlBonCollecte,
                        urlBonLivraison));
    }

    // DELETE /api/preuve/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {preuveRealisationService.supprimerPreuve(id);

        return ResponseEntity.ok().build();
    }
}