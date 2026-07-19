package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.ConfigPrestataire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigPrestataireRepository extends JpaRepository<ConfigPrestataire,Long> {
    Optional<ConfigPrestataire> findByPrestataireId(
            Long prestataireId);
}
