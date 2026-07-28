package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.EvaluationDTO;
import com.serviprox.serviprox_backend.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    // POST /api/evaluation
    // Client laisse une note après prestation
    // Note de 1 à 5 + commentaire

    @PostMapping
    public ResponseEntity<EvaluationDTO> creer(@RequestBody EvaluationDTO dto){

        return ResponseEntity.ok(evaluationService.creerEvaluation(dto));
    }

    // GET /api/evaluation/prestataire/{id}
    // Notes d'un prestataire
    // Flutter affiche les étoiles
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<EvaluationDTO>>getByPrestataire(@PathVariable Long prestataireId){

        return ResponseEntity.ok(evaluationService.getByPrestataire(prestataireId));
    }

    // GET /api/evaluation/client/{id}
    // Évaluations laissées par un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<EvaluationDTO>> getByClient(@PathVariable Long cliendId){

        return  ResponseEntity.ok(evaluationService.getByClient(cliendId));
    }
}
