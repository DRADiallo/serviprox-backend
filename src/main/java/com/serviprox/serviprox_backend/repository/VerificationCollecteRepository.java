package com.serviprox.serviprox_backend.repository;
import com.serviprox.serviprox_backend.entity.VerificationCollecte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCollecteRepository extends JpaRepository<VerificationCollecte, Long> {

    Optional<VerificationCollecte> findByCommandeId(Long commandeId);
}
