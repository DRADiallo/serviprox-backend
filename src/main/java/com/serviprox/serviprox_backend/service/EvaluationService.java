package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Evaluation;
import com.serviprox.serviprox_backend.repository.EvaluationRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final PrestataireRepository prestataireRepository;


    // Creer une evaluatin
    public Evaluation creerEvaluation(Evaluation evaluation){
        Evaluation saved= evaluationRepository.save(evaluation);

        // Recalculer note moyenne du prestataire
        List<Evaluation> evals = evaluationRepository
                .findByPrestataireId(evaluation.getPrestataire().getId());
        double moyenne = evals.stream()
                .mapToInt(Evaluation::getNote)
                .average()
                .orElse(0.0);
        var prestaire = prestataireRepository
                .findById(evaluation.getPrestataire().getId())
                .orElseThrow();
        prestaire.setNoteMoyenne(moyenne);
        prestataireRepository.save(prestaire);

        return  saved;
    }
    // Evaluation
    public List<Evaluation> getByPrestataire(Long prestataireId){
        return evaluationRepository.findByPrestataireId(prestataireId);

    }
}
