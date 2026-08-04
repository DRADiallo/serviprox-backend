package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.EvaluationDTO;
import com.serviprox.serviprox_backend.dto.PrestataireDTO;
import com.serviprox.serviprox_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// URL de base : /api/admin
// Accessible ADMIN uniquement
// Interface React Web
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private  final AdminService adminService;

    // GET /api/admin/dashboard
    // Statistiques tableau de bord React
    @GetMapping("/dashbord")
    public ResponseEntity<Map<String,Object>>dashbord(){

        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // GET /api/admin/statistiques
    // Statistiques avancées pour graphiques
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String,Object>>statistiques(){

        return ResponseEntity.ok(adminService.getStatistiquesAvancees());
    }

    // GET /api/admin/prestataires
    // Tous les prestataires
    @GetMapping("/prestataires")
    public ResponseEntity<List<PrestataireDTO>>getAllPrestataires(){

        return ResponseEntity.ok(adminService.getAllPrestataires());
    }

    // GET /api/admin/prestataires/en-attente
    // Prestataires en attente de validation
    @GetMapping("/prestataires/en-attente")
    public ResponseEntity<List<PrestataireDTO>>getAllPrestatairesEnAttente(){

        return ResponseEntity.ok(adminService.getPrestatairesEnAttente());
    }

    // PUT /api/admin/prestataires/{id}/valider
    // Admin valide un prestataire
    @PutMapping("/prestataires/{id}/valider")
    public ResponseEntity<PrestataireDTO>validerPrestataire(@PathVariable Long id){

        return ResponseEntity.ok(adminService.validerPrestataire(id));
    }

    // PUT /api/admin/prestataires/{id}/rejeter
    // Admin rejette un prestataire
    @PutMapping("/prestataires/{id}/rejeter")
    public ResponseEntity<PrestataireDTO>rejeterPrestataire(@PathVariable Long id,@RequestParam String motif){

        return ResponseEntity.ok(adminService.rejeterPrestataire(id ,motif));
    }


    // PUT /api/admin/prestataires/{id}/suspendre
    // Admin suspend un prestataire
    @PutMapping("/prestataires/{id}/suspendre")
    public ResponseEntity<PrestataireDTO>suspendrePrestataire(@PathVariable Long id,@RequestParam String motif){

        return ResponseEntity.ok(adminService.suspendrePrestataire(id,motif));
    }

    // PUT /api/admin/prestataires/{id}/reactiver
    @PutMapping("/prestataires/{id}/reactiver")
    public ResponseEntity<PrestataireDTO>reactiverPrestataire(@PathVariable Long id){

        return ResponseEntity.ok(adminService.ractiverPrestataire(id));
    }

    // GET /api/admin/commandes
    // Toutes les commandes
    @GetMapping("/commandes")
    public ResponseEntity<List<CommandeDTO>>getAllCommandes(){

        return ResponseEntity.ok(adminService.getAllCommandes());
    }

    // GET /api/admin/litiges
    // Tous les litiges en cours
    @GetMapping("/commandes/litiges")
    public ResponseEntity<List<CommandeDTO>>getAllCommandesLitiges(){

        return ResponseEntity.ok(adminService.getAllCommandesLitiges());
    }

    // GET /api/admin/evaluations
    // Toutes les évaluations — modération
    @GetMapping("/evaluations")
    public ResponseEntity<List<EvaluationDTO>>getAllEvalutions(){

        return ResponseEntity.ok(adminService.getAllEvalutions());
    }


    // DELETE /api/admin/evaluations/{id}
    // Supprimer évaluation abusive
    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void>supprimerEvaluation(@PathVariable Long id){

        adminService.supprimerEvalution(id);
        return ResponseEntity.ok().build();
    }








}
