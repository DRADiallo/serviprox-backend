package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.PreuveRealisationDTO;
import com.serviprox.serviprox_backend.service.PreuveRealisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// URL : /api/preuve
// Système de double preuve — pressing sénégalais
// Photo collecte + Photo livraison
// Comparaison automatique
@RestController
@RequestMapping("/api/preuve")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PreuveRealisationController {
    private final PreuveRealisationService preuveRealisationService;

    // POST /api/preuve/commande/{commandeId}
    // Agent ajoute une preuve
    // PHOTO, VIDEO ou SIGNATURE
    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<PreuveRealisationDTO>ajouter(@PathVariable Long commandeId,
                                                       @RequestParam String typePreuve,
                                                       @RequestParam String contenuUrl,
                                                       @RequestParam Long agentId){
        return  ResponseEntity.ok(preuveRealisationService.ajouterPreuve(
                commandeId,typePreuve,contenuUrl,agentId
        ));




    }
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<PreuveRealisationDTO>getByCommande(@PathVariable Long commandeId){

        return ResponseEntity.ok(preuveRealisationService.getByCommande(commandeId));

    }

    // GET /api/preuve/{id}
    @GetMapping
    public ResponseEntity<PreuveRealisationDTO>getById(@PathVariable Long id){
        return  ResponseEntity.ok(preuveRealisationService.getById(id));
    }

    // GET /api/preuve
    // Admin — toutes les preuves
    @GetMapping
    public ResponseEntity<List<PreuveRealisationDTO>>getAll(){

        return  ResponseEntity.ok(preuveRealisationService.getAll());
    }

    // POST /api/preuve/comparer/{commandeId}
    // Comparer bon collecte vs bon livraison
    // Innovation pressing ServiProx !
    @PostMapping("/comparer/comparerId}")
    public ResponseEntity<Boolean>comparer(@PathVariable Long commandeId,
                                           @RequestParam String urlBonColllecte,
                                           @RequestParam String urlBonLivraison){

        return ResponseEntity.ok(preuveRealisationService.comparerBons(commandeId,urlBonColllecte,urlBonLivraison));
    }

    // DELETE /api/preuve/{id}
    // Admin uniquement — données légales !
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>supprimer(@PathVariable Long id){
        preuveRealisationService.supprimer(id);
        return ResponseEntity.ok().build();
    }



}
