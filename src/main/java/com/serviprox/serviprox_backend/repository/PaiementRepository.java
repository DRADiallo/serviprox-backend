package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Paiement;
import com.serviprox.serviprox_backend.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement,Long> {
    Optional<Paiement> findByCommandeId(Long commandeId);
    List<Paiement> findByStatut(StatutPaiement statut);
}
