package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.PaiementDTO;
import com.serviprox.serviprox_backend.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaiementController {

    private  final PaiementService paiementService;

    // POST /api/paiement/commande/{commandeId}
    // Client initie le paiement
    // ESPECES, MOBILE_MONEY

    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<PaiementDTO> creer(@PathVariable Long commandeId, @RequestParam Double montant, @RequestParam String modePaiement, @RequestParam(required = false)
            String captureBase64,
            @RequestParam(required = false)
            String reference) {

        return ResponseEntity.ok(
                paiementService.creerPaiement(
                        commandeId, montant,
                        modePaiement,
                        captureBase64, reference));
    }

    @PutMapping("/{id}/confirmer-prestataire")
    public ResponseEntity<PaiementDTO>
    confirmerPrestataire(@PathVariable Long id) {

        return ResponseEntity.ok(
                paiementService.confirmerPaiement(id));
    }

    // GET /api/paiement/commande/{commandeId}
    // Voir paiement d'une commande
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<PaiementDTO>getByCommande(@PathVariable Long commandeId){

        return ResponseEntity.ok(paiementService.getByCommande(commandeId));
    }

    // GET /api/paiement/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(paiementService.getById(id));
    }
    // GET tous les paiements
    @GetMapping
    public ResponseEntity<List<PaiementDTO>>getAll(){

        return ResponseEntity.ok(paiementService.getAll());
    }

    // PUT /api/paiement/{id}/confirmer
    // Confirmer le paiement
    // → commande clôturée

    @PutMapping("/{id}/confirmer")
    public ResponseEntity<PaiementDTO>confirmer(@PathVariable Long id){

        return ResponseEntity.ok(paiementService.confirmerPaiement(id));
    }

    // PUT /api/paiement/{id}/rembourser
    // En cas de litige résolu
    // en faveur du client
    @PutMapping("/{id}/rembourser")
    public ResponseEntity<PaiementDTO>rembourser(@PathVariable Long id){

        return ResponseEntity.ok(paiementService.rembourser(id));
    }






}
