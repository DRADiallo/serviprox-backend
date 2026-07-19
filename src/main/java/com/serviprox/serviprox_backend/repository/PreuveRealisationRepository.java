package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreuveRealisationRepository extends JpaRepository<PreuveRealisation,Long> {
    Optional<PreuveRealisation> findByCommandeId(Long commandeId);
}
