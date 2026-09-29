package com.serviprox.serviprox_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.serviprox.serviprox_backend.dto.*;
import com.serviprox.serviprox_backend.service
        .VerificationCollecteService;

@RestController
@RequestMapping("/api/verification")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VerificationCollecteController {

    private final VerificationCollecteService verificationService;

    // ═══════════════════════════════════════
    // POST /api/verification/{commandeId}/creer
    // Créer vérification quand EN_VERIFICATION
    // ═══════════════════════════════════════
    @PostMapping("/{commandeId}/creer")
    public ResponseEntity<VerificationCollecteDTO> creer(@PathVariable Long commandeId) {

        return ResponseEntity.ok(verificationService.creerVerification(commandeId));
    }

    // ═══════════════════════════════════════
    // POST /api/verification/{commandeId}/confirmer
    // PRESTATAIRE ou CLIENT confirme
    // acteurType = PRESTATAIRE ou CLIENT
    // statut = OK ou PROBLEME
    // ═══════════════════════════════════════
    @PostMapping("/{commandeId}/confirmer")
    public ResponseEntity<CommandeDTO> confirmer(@PathVariable Long commandeId, @RequestParam String acteurType,
                                                 @RequestParam(defaultValue = "OK") String statut,@RequestParam(required = false)
                                                 String commentaire,
                                                 @RequestParam Long acteurId) {

        return ResponseEntity.ok(
                verificationService.confirmer(
                        commandeId, acteurType,
                        statut, commentaire, acteurId));
    }

    // ═══════════════════════════════════════
    // GET /api/verification/{commandeId}
    // Voir état de la vérification
    // ═══════════════════════════════════════
    @GetMapping("/{commandeId}")
    public ResponseEntity<VerificationCollecteDTO> getByCommande(@PathVariable Long commandeId) {

        return ResponseEntity.ok(verificationService.getByCommande(commandeId));
    }
}
