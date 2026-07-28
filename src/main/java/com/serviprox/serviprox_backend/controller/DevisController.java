package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.DevisDTO;
import com.serviprox.serviprox_backend.service.DevisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService devisService;

    // POST /api/devis/commande/{commandeId}
    // Technicien crée un devis après diagnostic

    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<DevisDTO>creer(@PathVariable Long commandeId, @RequestParam Double montantEstime){

        return  ResponseEntity.ok(devisService.creerDevis(commandeId,montantEstime));
    }

    // GET /api/devis/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DevisDTO>getById(@PathVariable Long id){

        return  ResponseEntity.ok(devisService.getById(id));
    }

    // GET /api/devis/commande/{commandeId}
    // Client voit son devis
    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<DevisDTO>getByCommande(@PathVariable Long commandeId){

        return  ResponseEntity.ok(devisService.getByCommande(commandeId));
    }
    // GET /api/devis
    // Admin — tous les devis
    @GetMapping
    public ResponseEntity<DevisService> getAll(){

        return  ResponseEntity.ok(devisService.getAll());
    }

    // PUT /api/devis/{id}/valider
    // Client accepte le devis
    // → réparation peut commencer
    @PostMapping("/{id}/valider")
    public ResponseEntity<DevisDTO>valider(@PathVariable Long id){

        return  ResponseEntity.ok(devisService.validerDevis(id));
    }

    // PUT /api/devis/{id}/refuser
    // Client refuse le devis
    // → commande annulée
    @PostMapping("/{id}/refuser")
    public ResponseEntity<DevisDTO>refuser(@PathVariable Long id){

        return  ResponseEntity.ok(devisService.refuserDevis(id));
    }






}
