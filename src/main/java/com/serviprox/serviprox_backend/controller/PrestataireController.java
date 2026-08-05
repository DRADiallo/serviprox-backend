package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.PrestataireDTO;
import com.serviprox.serviprox_backend.service.PrestataireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/prestataire")
@CrossOrigin(origins ="*" )
@RequiredArgsConstructor
public class PrestataireController {
    private final PrestataireService prestataireService;

    // GET /api/prestataire
    // Flutter — liste prestataires actifs
    // Client cherche un pressing ou technicien

    @GetMapping
    public ResponseEntity<List<PrestataireDTO>> getAllPrestatairesActiffs(){

        return ResponseEntity.ok(prestataireService.getAllActifs());
    }

    // GET /api/prestataire/tous
    // Admin React — tous les prestataires
    @GetMapping("/tous")
    public ResponseEntity<List<PrestataireDTO>>getAll(){

        return ResponseEntity.ok(prestataireService.getAll());
    }

    // GET /api/prestataire/{id}
    // Flutter — détail d'un prestataire
    @GetMapping("/{id}")
    public ResponseEntity<PrestataireDTO>getById(@PathVariable Long id){

        return ResponseEntity.ok(prestataireService.getById(id));
    }



    // GET /api/prestataire/zone?zone=Médina
    // Flutter — prestataires par zone
    // Client cherche dans son quartier
    @GetMapping("/zone")
    public ResponseEntity<List<PrestataireDTO>>getByZone(@RequestParam String zone ){

        return ResponseEntity.ok(prestataireService.getByZone(zone));
    }
    // PUT /api/prestataire/{id}
    // Prestataire modifie son profil
    @PutMapping("/{id}")
    public ResponseEntity<PrestataireDTO>modifier(@PathVariable Long id, @RequestBody PrestataireDTO dto){

        return ResponseEntity.ok(prestataireService.modifier(id,dto));
    }

    // PUT /api/prestataire/{id}/desactiver
    // Admin désactive un prestataire
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<PrestataireDTO> desactiver(@PathVariable Long id, @RequestParam String motif){

        return ResponseEntity.ok(prestataireService.desactiver(id, motif));
    }

    // PUT /api/prestataire/{id}/suspendre
    // Admin suspend temporairement
    @PutMapping("/{id}/susprendre")
    public ResponseEntity<PrestataireDTO>susprendre(@PathVariable Long id){

        return ResponseEntity.ok(prestataireService.suspendre(id));
    }

    // PUT /api/prestataire/{id}/reactiver
    // Admin réactive après suspension
    @PutMapping("/{id}/reactiver")
    public ResponseEntity<PrestataireDTO> reactiver(@PathVariable Long id){

        return ResponseEntity.ok(prestataireService.reactiver(id));
    }



}
