package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffectationRepository extends JpaRepository<Affectation,Long> {
    List<Affectation> findByAgentId(Long agentId);
    List<Affectation> findByCommandeId(Long commandeId);
    //  chercher par type
    Optional<Affectation>
    findByCommandeIdAndTypeAffectation(
            Long commandeId,
            String typeAffectation);
}
