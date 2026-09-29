package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface OtpRepository
        extends JpaRepository<OtpCode, Long> {

    // Chercher OTP valide par téléphone
    Optional<OtpCode> findByTelephoneAndUtiliseFalse(
            String telephone);

    // Supprimer anciens OTPs du même téléphone
    @Transactional
    void deleteByTelephone(String telephone);
}