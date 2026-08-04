package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.entity.BonCollectRequest;
import com.serviprox.serviprox_backend.entity.ComparaisonResultat;
import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.service.BonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// URL : /api/bon
// Gestion bons collecte et livraison
// Système hybride : MANUEL ou QR_CODE
@RestController
@RequestMapping("/api/bon")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BonController {

    private  final BonService bonService;

    // POST /api/bon/generer
    // Agent génère un bon
    // Mode MANUEL → compteurs visuels Flutter
    // Mode QR_CODE → scan étiquettes (Chap 8)
    @PostMapping("/generer")
    public ResponseEntity<PreuveRealisation>generer(@RequestBody BonCollectRequest request){

        return ResponseEntity.ok(bonService.genererBon(request));
    }

    // POST /api/bon/comparer/{commandeId}
    // Comparer bon collecte vs livraison
    // IA JSON → fallback Google Vision (Chap 8)

    @PostMapping("/comparer/{commandeId}")
    public ResponseEntity<ComparaisonResultat>comparer(@PathVariable Long commandeId,
                                                       @RequestParam String jsonBonCollecte,
                                                       @RequestParam String jsonBonLivraison){

        return ResponseEntity.ok(bonService.comparerBons(commandeId,jsonBonCollecte,jsonBonLivraison));
    }



}
