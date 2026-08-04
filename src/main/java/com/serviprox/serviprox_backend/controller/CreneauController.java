package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.dto.CreneauDTO;
import com.serviprox.serviprox_backend.service.CreneauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// URL : /api/creneau
// Planification soins à domicile
// Date + heure + durée OBLIGATOIRES
@RestController
@RequestMapping("/api/creneau")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CreneauController {
    private  final CreneauService creneauService;

    // POST /api/creneau
    // Créer un créneau pour soins domicile
    // Date + heure + durée obligatoires
    @PostMapping
    public ResponseEntity<CreneauDTO>creer(@RequestParam LocalDateTime dateDebut,
                                           @RequestParam LocalDateTime dateFin){

        return ResponseEntity.ok(creneauService.creerCreneau(dateDebut,dateFin));
    }

    // GET /api/creneau/disponibles
    // Flutter affiche créneaux libres
    // Client choisit son horaire
    @GetMapping("/disponibles")
    public ResponseEntity<List<CreneauDTO>>getDisponibles(){

        return ResponseEntity.ok(creneauService.getDisponibles());
    }

    // GET /api/creneau/reserves
    // Créneaux déjà pris
    // Calendrier prestataire
    @GetMapping("/reserves")
    public ResponseEntity<List<CreneauDTO>>getReserves(){

        return ResponseEntity.ok(creneauService.getReserves());
    }

    // GET /api/creneau/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CreneauDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(creneauService.getById(id));
    }

    // GET /api/creneau
    // Admin — tous les créneaux
    @GetMapping
    public ResponseEntity<List<CreneauDTO>>getAll(){

        return ResponseEntity.ok(creneauService.getAll());
    }

    // PUT /api/creneau/{id}/reserver
    // Client réserve un créneau
    // LIBRE → RESERVE
    @GetMapping("/{id}/reserver")
    public ResponseEntity<CreneauDTO>reserver(@PathVariable Long id){

        return ResponseEntity.ok(creneauService.reserverCreneau(id));
    }

    // PUT /api/creneau/{id}/liberer
    // Libérer si commande annulée
    // RESERVE → LIBRE
    @PutMapping("/{id}/liberer")
    public ResponseEntity<CreneauDTO>liberer(@PathVariable Long id){

        return ResponseEntity.ok(creneauService.libererCreneau(id));
    }

    // DELETE /api/creneau/{id}
    // Supprimer créneau LIBRE uniquement
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id){

        creneauService.supprimer(id);
        return ResponseEntity.ok().build();
    }





}
