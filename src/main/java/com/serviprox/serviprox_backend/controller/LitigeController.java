package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.service.LitigeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/litige")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LitigeController {

    private  final LitigeService litigeService;

    // POST /api/litige/{commandeId}/signaler
    // Client signale un problème
    // Après fin de prestation uniquement
    @PostMapping("/{commandeId}/signaler")
    public ResponseEntity<CommandeDTO>signalerLitige(@PathVariable Long commandeId, @RequestParam String motif, @RequestParam Long clientId){

        return ResponseEntity.ok(litigeService.signalerLitige(commandeId,motif,clientId));
    }

    // PUT /api/litige/{commandeId}/arbitrer
    // Prestataire prend une décision
    // RESOLU ou REMBOURSE
    @PutMapping("/{commandeId}/arbitrer")
    public ResponseEntity<CommandeDTO>arbitrerLitige(@PathVariable Long commandeId, @RequestParam String decision, @RequestParam Long prestataireId){

        return ResponseEntity.ok(litigeService.arbitrerLitige(commandeId,decision,prestataireId));
    }

    // GET /api/litige/prestataire/{id}
    // Litiges d'un prestataire

    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<CommandeDTO>> getByPrestataire(@PathVariable Long prestataireId){

        return ResponseEntity.ok(litigeService.getLitigesPrestataire(prestataireId));
    }

    // GET /api/litige/client/{id}
    // Litiges d'un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeDTO>> getByClient(@PathVariable Long clientId){

        return ResponseEntity.ok(litigeService.getLitigesClient(clientId));
    }

    // GET /api/litige
    // Admin — tous les litiges
    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getAll(){

        return ResponseEntity.ok(litigeService.getAllLitiges());
    }



}
