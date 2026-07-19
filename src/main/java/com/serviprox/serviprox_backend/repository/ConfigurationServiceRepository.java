package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.ConfigurationService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfigurationServiceRepository extends JpaRepository<ConfigurationService,Long> {

    Optional<ConfigurationService> findByTypeService(
            String typeService);

    // Uniquement les services actifs
    // Flutter affiche seulement ceux-là au client
    List<ConfigurationService> findByActifTrue();

    // Tous les services (admin React)
    // incluant les désactivés
    List<ConfigurationService>findAll();
}
