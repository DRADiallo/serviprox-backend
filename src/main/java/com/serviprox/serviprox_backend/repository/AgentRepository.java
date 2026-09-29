package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent,Long> {
    List<Agent> findByPrestataireId(Long prestataireId);
    List<Agent> findByDisponibiliteTrue();
    //  AJOUT : chercher agent par téléphone
    Optional<Agent> findByTelephone(
            String telephone);
    // ← Vérifier si téléphone existe
    boolean existsByTelephone(String telephone);
}
