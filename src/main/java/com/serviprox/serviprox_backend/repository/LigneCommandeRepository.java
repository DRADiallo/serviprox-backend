package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande,Long> {
        List<LigneCommande>findByCommandeId(Long CommandeId);
}
