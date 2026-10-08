package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.CommandeRequest;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.service.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commande")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    // POST /api/commande/client/{clientId}
    // Client passe une commande
    @PostMapping("/client/{clientId}")
    public ResponseEntity<CommandeDTO> creer (@PathVariable Long clientId, @RequestBody CommandeRequest req){

        return ResponseEntity.ok(commandeService.creerCommande(clientId,req));
    }

    // GET /api/commande/{id}
    // Détail d'une commande
    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(commandeService.getById(id));
    }

    // GET /api/commande/client/{clientId}
    // Historique commandes du client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeDTO>> getByClient(@PathVariable Long clientId){

        return ResponseEntity.ok(commandeService.getByClient(clientId));
    }

    // GET /api/commande/prestataire/{prestataireId}
    // Tableau de bord prestataire
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<CommandeDTO>>getByPrestataire(@PathVariable Long prestataireId){

        return ResponseEntity.ok(commandeService.getByPrestataire(prestataireId));
    }

    // GET /api/commande/statut/{statut}
    // Filtrer par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CommandeDTO>>getByStatut(@PathVariable StatutCommande statut){

        return  ResponseEntity.ok(commandeService.getByStatut(statut));
    }

    // GET /api/commande
    // Admin — toutes les commandes
    @GetMapping
    public ResponseEntity<List<CommandeDTO>>getAll(){

        return  ResponseEntity.ok(commandeService.getAll());
    }

    // PUT /api/commande/{id}/statut
    // Changer statut d'une commande
    // Prestataire accepte, Agent démarre...
    @PutMapping("/{id}/statut")
    public ResponseEntity<CommandeDTO>changerStatut(@PathVariable Long id, @RequestParam String statut,
                                                    @RequestParam Long acteurId){

        return  ResponseEntity.ok(commandeService.actuliserStatut(id,statut,acteurId));
    }

    // PUT /api/commande/{id}/annuler
    // Client ou prestataire annule
    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeDTO>annuler(@PathVariable Long id, @RequestParam Long acteurId){

        return  ResponseEntity.ok(commandeService.annuler(id,acteurId));
    }

    //  : assigner agent
    // PUT /api/commande/{id}/assigner-agent
    @PutMapping("/{id}/assigner-agent")
    public ResponseEntity<CommandeDTO> assignerAgent(@PathVariable Long id, @RequestParam Long agentId,@RequestParam String typeAffectation) {
        return ResponseEntity.ok(
                commandeService.assignerAgent(
                        id, agentId,typeAffectation));
    }

    // ← AJOUT : Prestataire fixe prix réel
// PUT /api/commande/{id}/fixer-prix
    @PutMapping("/{id}/fixer-prix")
    public ResponseEntity<CommandeDTO>
    fixerPrixReel(
            @PathVariable Long id,
            @RequestParam Double prixPrestations,
            @RequestParam(required = false,
                    defaultValue = "0")
            Double prixCollecte,
            @RequestParam(required = false,
                    defaultValue = "0")
            Double prixLivraison,
            @RequestParam(required = false)
            String detailPrix,
            @RequestParam Long acteurId) {

        return ResponseEntity.ok(
                commandeService.fixerPrixReel(
                        id,
                        prixPrestations,
                        prixCollecte,
                        prixLivraison,
                        detailPrix,
                        acteurId));
    }

    // ← AJOUT : Client accepte/refuse prix réel
// PUT /api/commande/{id}/accepter-prix
    @PutMapping("/{id}/accepter-prix")
    public ResponseEntity<CommandeDTO>
    accepterPrix(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam Long acteurId) {

        return ResponseEntity.ok(
                commandeService.accepterPrix(
                        id, decision, acteurId));
    }
}
