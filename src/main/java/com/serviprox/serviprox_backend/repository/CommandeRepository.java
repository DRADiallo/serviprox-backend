package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande,Long> {
    List<Commande> findByClientId(Long clientId);
    List<Commande> findByPrestataireId(Long prestataireId);
    List<Commande> findByStatutCommande(StatutCommande statut);
    List<Commande> findByPrestataireIdAndStatutCommande(
            Long prestataireId, StatutCommande statut);
}
