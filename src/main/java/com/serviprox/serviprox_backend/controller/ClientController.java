package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.ClientDTO;
import com.serviprox.serviprox_backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClientController {
    // injection ClientService
    private final ClientService clientService;

    // ═══════════════════════════════════════
    // GET /api/client
    // Admin React — liste tous les clients
    // ═══════════════════════════════════════
    // @GetMapping → requête HTTP GET
    // Utilisé pour LIRE des données
    // Sans modifier quoi que ce soit

    @GetMapping
    public ResponseEntity<List<ClientDTO>>getAll(){

        return ResponseEntity.ok(clientService.getAll());
    }

    // ═══════════════════════════════════════
    // GET /api/client/{id}
    // Flutter — voir mon profil
    // ═══════════════════════════════════════
    // @PathVariable → valeur dans l'URL
    // Ex: GET /api/client/1
    // → id = 1
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(clientService.getById(id));
    }

    // ═══════════════════════════════════════
    // PUT /api/client/{id}
    // Flutter — modifier mon profil
    // ═══════════════════════════════════════
    // @PutMapping → requête HTTP PUT
    // Utilisé pour MODIFIER une ressource
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO>modifier(@PathVariable Long id, @RequestBody ClientDTO dto){

        return ResponseEntity.ok(clientService.modifier(id,dto));
    }


    // ═══════════════════════════════════════
    // PUT /api/client/{id}/desactiver
    // Admin — désactiver un client abusif
    // ═══════════════════════════════════════
    // On ne SUPPRIME jamais un client
    // On le désactive uniquement
    // Ses commandes historiques sont conservées

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<ClientDTO>desactiver(@PathVariable Long id){

        return ResponseEntity.ok(clientService.desactiver(id));
    }

    // ═══════════════════════════════════════
    // PUT /api/client/{id}/reactiver
    // Admin — réactiver un client
    // ═══════════════════════════════════════
    @PutMapping("/{id}/reactiver")
    public ResponseEntity<ClientDTO> reactiver(@PathVariable Long id){

        return ResponseEntity.ok(clientService.reactiver(id));
    }






}
