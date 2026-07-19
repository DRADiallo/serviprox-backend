package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Creneau;
import com.serviprox.serviprox_backend.enums.StatutCreneau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau,Long> {
    List<Creneau> findByStatut(StatutCreneau statut);
}
