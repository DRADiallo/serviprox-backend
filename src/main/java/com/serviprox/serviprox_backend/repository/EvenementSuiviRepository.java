package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.EvenementSuivi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvenementSuiviRepository extends JpaRepository<EvenementSuivi,Long> {
    List<EvenementSuivi> findByCommandeId(Long commandeId);
}
