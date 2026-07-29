package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.dto.AffectationDTO;
import com.serviprox.serviprox_backend.service.AffectationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AffectationController {

    private  final AffectationService affectationService;

    // POST /api/affectation
    // Prestataire affecte un agent
    // à une commande
    @PostMapping
    public ResponseEntity<AffectationDTO>affecter(@RequestParam Long commandeId, @RequestParam Long agentId){

        return ResponseEntity.ok(affectationService.affecterAgent(commandeId,agentId));

    }

    // GET /api/affectation/commande/{id}
    // Quel agent est assigné à cette commande ?
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<AffectationDTO>getByCommande(@PathVariable Long agentId){

        return ResponseEntity.ok(affectationService.getByAgent(agentId));
    }

    // PUT /api/affectation/{id}/statut
    // Changer statut affectation
    @PutMapping("/{id}/statut")
    public ResponseEntity<AffectationDTO>changerStatut(@PathVariable Long id, @RequestParam String statut){

        return ResponseEntity.ok(affectationService.actualiserStatu(id,statut));
    }

    // PUT /api/affectation/liberer/{agentId}
    // Libérer un agent après mission
    @PutMapping("/liberer/{agentId}")
    public ResponseEntity<Void>liberer(@PathVariable Long agentId){

         affectationService.libererAgent(agentId);
         return ResponseEntity.ok().build();
    }




}
