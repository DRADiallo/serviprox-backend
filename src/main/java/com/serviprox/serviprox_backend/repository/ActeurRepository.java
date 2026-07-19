package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Acteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActeurRepository extends JpaRepository<Acteur , Long> {
    Optional<Acteur> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
