package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository
        extends JpaRepository<OtpCode, Long> {

    // Chercher OTP valide par téléphone
    Optional<OtpCode> findByTelephoneAndUtiliseFalse(
            String telephone);

    // Supprimer anciens OTPs du même téléphone
    void deleteByTelephone(String telephone);
}