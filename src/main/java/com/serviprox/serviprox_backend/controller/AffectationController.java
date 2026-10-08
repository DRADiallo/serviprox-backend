package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.dto.AffectationDTO;
import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.service.AffectationService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     // Get /api/affectation/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AffectationDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(affectationService.getById(id));
    }

    // GET /api/affectation/agent/{agentId}
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AffectationDTO>>getByAgent(@PathVariable Long agentId){

        return ResponseEntity.ok(affectationService.getByAgent(agentId));
    }


    // GET /api/affectation/commande/{id}
    // Quel agent est assigné à cette commande ?
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<AffectationDTO>>getByCommande(@PathVariable Long commandeId){

        return ResponseEntity.ok(affectationService.getByAgent(commandeId));
    }

    // PUT /api/affectation/{id}/statut
    // Changer statut affectation
    @PutMapping("/{id}/statut")
    public ResponseEntity<AffectationDTO>actualiserStatut(@PathVariable Long id, @RequestParam String statut){

        return ResponseEntity.ok(affectationService.actualiserStatut(id,statut));
    }

    // PUT /api/affectation/liberer/{agentId}
    // Libérer un agent après mission
    @PutMapping("/liberer/{agentId}")
    public ResponseEntity<Void>liberer(@PathVariable Long agentId){

         affectationService.libererAgent(agentId);
         return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>annulerAffectation(@PathVariable Long id){

        affectationService.annulerAffectation(id);
        return ResponseEntity.ok().build();
    }

    // ← AJOUT : Agent confirme/refuse mission
    @PutMapping("/commande/{commandeId}/confirmer")
    public ResponseEntity<AffectationDTO>
    confirmerMission(@PathVariable Long commandeId, @RequestParam Long agentId, @RequestParam String decision) {

        return ResponseEntity.ok(
                affectationService.confirmerMission(
                        commandeId, agentId, decision));
    }

    // ← AJOUT : Missions d'un agent
//    @GetMapping("/agent/{agentId}")
//    public ResponseEntity<List<AffectationDTO>> getMissionsAgent(@PathVariable Long agentId) {
//
//        return ResponseEntity.ok(affectationService.getMissionsAgent(agentId));
//    }

    // en - attente
    @GetMapping("/agent/{agentId}/en-attente")
    public ResponseEntity<List<AffectationDTO>>
    getMissionsEnAttente(@PathVariable Long agentId) {
        return ResponseEntity.ok(affectationService.getMissionsEnAttente(agentId));
    }



}
