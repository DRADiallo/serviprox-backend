package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention,Long> {
    List<Intervention> findByCommandeId(Long commandeId);
}
