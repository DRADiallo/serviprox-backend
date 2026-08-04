package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.EvenementSuiviDTO;
import com.serviprox.serviprox_backend.service.EvenementSuiviService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// URL : /api/evenement
// Timeline de suivi — comme un tracking colis
// Client voit chaque étape de sa commande
@RestController
@RequestMapping("/api/evenement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EvenementSuiviController {

    private final EvenementSuiviService evenementSuiviService;


    // GET /api/evenement/commande/{id}
    // Timeline complète d'une commande
    // Flutter affiche comme tracking colis
    // Admin React voit qui a fait quoi
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<EvenementSuiviDTO>>getHistorique(@PathVariable Long commandeId){

        return ResponseEntity.ok(evenementSuiviService.getHistorique(commandeId));
    }

    // GET /api/evenement/{id}

    @GetMapping("/{id}")
    public ResponseEntity<EvenementSuiviDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(evenementSuiviService.getById(id));
    }

    // GET /api/evenement
    // Admin — tous les événements

    @GetMapping()
    public ResponseEntity<List<EvenementSuiviDTO>>getAll(){

        return ResponseEntity.ok(evenementSuiviService.getAll());
    }

    // GET /api/evenement/type/{type}
    // Filtrer par type
    // Ex: tous les LITIGE pour audit
    @GetMapping("/type/{type}")
    public ResponseEntity<List<EvenementSuiviDTO>>getByType(@PathVariable String type){

        return ResponseEntity.ok(evenementSuiviService.getByType(type));
    }

    // DELETE /api/evenement/{id}
    // Admin uniquement — données légales !
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id){

        evenementSuiviService.supprimer(id);
        return ResponseEntity.ok().build();
    }
}
