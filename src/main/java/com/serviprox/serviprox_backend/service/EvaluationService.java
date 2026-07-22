package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.EvaluationDTO;
import com.serviprox.serviprox_backend.entity.Evaluation;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.EvaluationRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final PrestataireRepository prestataireRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;

    // ═══ CREATE ═══
    // Client note le prestataire
    // après clôture de la commande
    // Note de 1 à 5
    // Recalcul automatique moyenne prestataire

    public EvaluationDTO creerEvaluation(EvaluationDTO dto){

        // verifions que la note est valide (1-> 5)

        if (dto.getNote() < 1 || dto.getNote() > 5){

            throw new RuntimeException("La note doit  être entre 1 et 5");
        }

        var client= clientRepository
                .findById(dto.getClientId())
                .orElseThrow(()-> new RuntimeException("Client Introuvable"));
        var prestataire = prestataireRepository
                .findById(dto.getPrestataireId())
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));

        Evaluation evaluation= new Evaluation();
        evaluation.setClient(client);
        evaluation.setPrestataire(prestataire);
        evaluation.setNote(dto.getNote());
        evaluation.setCommentaire(dto.getCommentaire());
        evaluation.setDateEvaluation(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluation);
        // Recalculer note moyenne prestataire
        // Appelé après chaque nouvelle évaluation
        recalculerMoyenne(dto.getPrestataireId());

        // Notifier le prestataire
        notificationService.envoyer(
                dto.getPrestataireId(),
                "NOUVELLE_EVALUATION",
                "⭐ Nouvelle évaluation reçue : "
                        + dto.getNote() + "/5 — "
                        + (dto.getCommentaire() != null
                        ? dto.getCommentaire()
                        : "Sans commentaire")
        );
        return toDTO(saved);

    }

    // ═══ READ ALL ═══
    // Admin React — modération
    public List<EvaluationDTO>getAll(){

        return evaluationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    // ═══ READ BY ID ═══

    public EvaluationDTO getById(Long id){

        return toDTO(evaluationRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Evaluation Introuvable")));

    }

    // ═══ READ BY PRESTATAIRE ═══
    // Afficher les avis d'un prestataire
    // dans son profil Flutter
    public List<EvaluationDTO>getByPrestataire(Long prestataireId){

        return evaluationRepository.findByPrestataireId(prestataireId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY CLIENT ═══
    // Historique des évaluations d'un client

    public List<EvaluationDTO>getByClient(Long clientId){

        return evaluationRepository.findByPrestataireId(clientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ DELETE ═══
    // Admin supprime une évaluation abusive
    // Ex: commentaire irrespectueux
    //     fausse note concurrentielle
    public void supprimer(Long id){

        Evaluation eval= evaluationRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Evalution Introuvable"));

        Long prestataireId =eval.getPrestataire().getId();
        evaluationRepository.deleteById(id);
        // Recalculer la moyenne après suppression
        recalculerMoyenne(prestataireId);

    }

    // ═══ RECALCUL MOYENNE ═══
    // Appelé après chaque CREATE ou DELETE
    // Note moyenne mise à jour en temps réel

    private void recalculerMoyenne(Long prestataireId){

        List<Evaluation> evals = evaluationRepository.findByPrestataireId(prestataireId);

        // Moyenne de toutes les évaluations
        double moyenne= evals.stream()
                .mapToInt(Evaluation::getNote)
                .average()
                .orElse(0.0);

        var prestataire = prestataireRepository
                .findById(prestataireId)
                .orElseThrow();

        prestataire.setNoteMoyenne(moyenne);
        prestataireRepository.save(prestataire);

    }

    // ═══ MAPPER entité → DTO ═══
    public EvaluationDTO toDTO(Evaluation e) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(e.getId());
        dto.setNote(e.getNote());
        dto.setCommentaire(e.getCommentaire());
        if (e.getClient() != null) {
            dto.setClientId(e.getClient().getId());
            dto.setNomClient(
                    e.getClient().getNom()
                            + " " + e.getClient().getPrenom());
        }
        if (e.getPrestataire() != null) {
            dto.setPrestataireId(
                    e.getPrestataire().getId());
            dto.setNomPrestataire(
                    e.getPrestataire()
                            .getNomCommercial());
        }
        return dto;
    }


}
