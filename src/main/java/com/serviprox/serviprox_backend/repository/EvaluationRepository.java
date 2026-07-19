package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {
    List<Evaluation> findByPrestataireId(Long prestataireId);
    List<Evaluation> findByClientId(Long clientId);
}
