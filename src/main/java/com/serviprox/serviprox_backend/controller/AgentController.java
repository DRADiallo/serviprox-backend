package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.AgentDTO;
import com.serviprox.serviprox_backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    // POST /api/agent/prestataire/{prestataireId}
    // Prestataire crée un agent
    // L'agent ne s'inscrit pas lui-même !
    @PostMapping("/prestataire/{prestataireId}")
    public ResponseEntity<AgentDTO> creer(@PathVariable Long prestataireId, @RequestBody AgentDTO dto){

        return ResponseEntity.ok(agentService.creerAgent(dto,prestataireId));
    }

    // GET /api/agent
    // Admin — tous les agents
    @GetMapping
    public ResponseEntity<List<AgentDTO>> getAll(){
        return ResponseEntity.ok(agentService.getAll());
    }

    // GET /api/agent/{id}
    // Détail d'un agent
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO>getById(@PathVariable Long id){
        return ResponseEntity.ok(agentService.getById(id));
    }


    // GET /api/agent/prestataire/{prestataireId}
    // Prestataire voit ses agents
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<AgentDTO>> getByPrestataire(@PathVariable Long prestataireId){

        return ResponseEntity.ok(agentService.getByPrestataire(prestataireId));
    }

    // GET /api/agent/disponibles
    // Agents disponibles pour affectation
    @GetMapping("/disponibles")
    public ResponseEntity<List<AgentDTO>> getDisponible(){
        return ResponseEntity.ok(agentService.getAgentDisponible());
    }

    // PUT /api/agent/{id}
    // Modifier infos agent
    @PutMapping("/{id}/modifier")
    public ResponseEntity<AgentDTO> modifier(@PathVariable Long prestataireId, @RequestBody AgentDTO dto){

        return ResponseEntity.ok(agentService.modifier(prestataireId,dto));
    }

    // PUT /api/agent/{id}/disponibilite
    // Changer disponibilité agent
    // true → disponible / false → en mission
    @PutMapping("/{id}/disponibilite")
    public ResponseEntity<AgentDTO> ChangerDisponibilite(@PathVariable Long id, @RequestParam Boolean disponibilite){

        return ResponseEntity.ok(agentService.actualiserDisponibilites(id,disponibilite));
    }

    // PUT /api/agent/{id}/position
    // Mettre à jour position GPS
    // Appelé en temps réel pendant déplacement
    @PutMapping("/{id}/position")
    public ResponseEntity<AgentDTO> mettreAJourPosition(@PathVariable Long id, @RequestParam String position){

        return ResponseEntity.ok(agentService.actualiserPositionGPS(id,position));
    }

    // PUT /api/agent/{id}/desactiver
    // Désactiver un agent

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<AgentDTO> desactiver(@PathVariable Long id){

        return ResponseEntity.ok(agentService.desactiverAgent(id));
    }

    // PUT /api/agent/{id}/reactiver
    // Réactiver un agent

    @PutMapping("/{id}/reactiver")
    public ResponseEntity<AgentDTO> reactiver(@PathVariable Long id){

        return ResponseEntity.ok(agentService.reactiverAgent(id));
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

    // GET /api/agent/{id}/pin
// Prestataire voir le PIN de son agent
    @GetMapping("/{id}/pin")
    public ResponseEntity<Map<String, String>>voirPin(@PathVariable Long id) {
        return ResponseEntity.ok(
                agentService.getPinClair(id));
    }

    // PUT /api/agent/{id}/reset-pin
    // Prestataire réinitialise le PIN
    @PutMapping("/{id}/reset-pin")
    public ResponseEntity<Map<String, String>>resetPin(@PathVariable Long id) {
        return ResponseEntity.ok(
                agentService.resetPin(id));
    }





}
