package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.AgentDTO;
import com.serviprox.serviprox_backend.service.AgentService;
import com.serviprox.serviprox_backend.service.InterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// URL : /api/gps
// Suivi GPS en temps réel
// Option A — Polling (prototype)
// Flutter client interroge toutes les 5 secondes
@RestController
@RequestMapping("/api/gps")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GPSController {

    private final AgentService agentService;
    private final InterventionService interventionService;

    // PUT /api/gps/agent/{id}
    // Agent met à jour sa position
    // Appelé automatiquement par Flutter
    // toutes les 10 secondes
    // Format : "14.7167,-17.4677"
    @PutMapping("/agent/{agentId}")
    public ResponseEntity<AgentDTO>mettreAJourPosition(@PathVariable Long agenId,
                                                       @RequestParam String latitude,
                                                       @RequestParam String longtitude){
        // Combiner lat + lng
        String position = latitude + "," + longtitude;

        return ResponseEntity.ok(agentService.updatePositionGPS(agenId,position));
    }

    // GET /api/gps/agent/{id}
    // Client récupère position agent
    // Appelé par Flutter toutes les 5 secondes
    // → affiche sur Google Maps
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<String>getPosition(@PathVariable Long agentId){
        AgentDTO agent = agentService.getById(agentId);
        return ResponseEntity.ok(agentService.getPositionActuelle(agentId));
    }

    // PUT /api/gps/intervention/{id}
    // Mettre à jour position
    // via l'intervention active
    @PutMapping("/intervention/{interventionId}")
    public ResponseEntity<Void> mettreAjourIntervention(@PathVariable Long interventionId,
                                                        @RequestParam String latitude,
                                                        @RequestParam String longitude){
        String position = latitude + "," + longitude;

        interventionService.mettreAjourPositionGPS(interventionId,position);
        return ResponseEntity.ok().build();
    }
}
