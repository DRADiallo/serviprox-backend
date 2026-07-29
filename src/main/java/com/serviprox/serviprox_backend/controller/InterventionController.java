package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.InterventionDTO;
import com.serviprox.serviprox_backend.service.InterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intervention")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;


    // POST /api/intervention/commande/{id}
    // Agent démarre une intervention
    @PostMapping("/commande/{commandeId}")
    public ResponseEntity<InterventionDTO>demarrer(@PathVariable Long commandeId,
                                                   @RequestParam String adresseDepart,
                                                   @RequestParam String adresseArrive,
                                                   @RequestParam String typeIntervention){

        return ResponseEntity.ok(interventionService.demarrerIntervention(commandeId,adresseDepart,adresseArrive,typeIntervention));

    }

    // GET /api/intervention/commande/{id}
    // Interventions d'une commande
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<InterventionDTO>>getByCommande(@PathVariable Long commandeId){

        return  ResponseEntity.ok(interventionService.getByCommande(commandeId));
    }

    // GET /api/intervention/{id}
    @GetMapping("/{id}")
    public ResponseEntity<InterventionDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(interventionService.getById(id));
    }

    // PUT /api/intervention/{id}/terminer
    // Agent signale fin de mission
    @PutMapping("/{id}/terminer")
    public ResponseEntity<InterventionDTO>terminer(@PathVariable Long id, @RequestParam String typeIntervention){

        return ResponseEntity.ok(interventionService.terminerIntervention(id,typeIntervention));
    }

    // PUT /api/intervention/{id}/position
    // GPS temps réel — Dakar embouteillages !
    @PutMapping("/{id}/positionGPS")
    public ResponseEntity<InterventionDTO>mettreAjourPosition(@PathVariable Long id, @RequestParam String positionGPS){

        return ResponseEntity.ok(interventionService.mettreAjourPositionGPS(id,positionGPS));
    }


}
