package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreuveRealisationRepository extends JpaRepository<PreuveRealisation,Long> {
    List<PreuveRealisation> findByCommandeId(
            Long commandeId);

    Optional<PreuveRealisation>
    findByCommandeIdAndType(Long commandeId, TypePreuve type);
}
