package com.serviprox.serviprox_backend.repository;
import com.serviprox.serviprox_backend.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// ← CORRECTION : ajouter <FcmToken, Long>
public interface FcmTokenRepository
        extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUserIdAndRole(
            Long userId, String role);

    List<FcmToken> findByUserId(Long userId);

    void deleteByUserIdAndRole(
            Long userId, String role);
}
