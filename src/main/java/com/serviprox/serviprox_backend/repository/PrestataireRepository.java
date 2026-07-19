package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestataireRepository extends JpaRepository<Prestataire,Long> {
    Optional<Prestataire> findByEmail(String email);
    List<Prestataire> findByStatut(StatutActeur statut);
    List<Prestataire> findByZoneCouverture(String zone);
}
