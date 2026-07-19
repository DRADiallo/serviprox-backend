package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Devis;
import com.serviprox.serviprox_backend.enums.StatutDevis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DevisRepository extends JpaRepository<Devis,Long> {
    Optional<Devis> findByCommandeId(Long commandeId);
    List<Devis> findByStatut(StatutDevis statut);
}
