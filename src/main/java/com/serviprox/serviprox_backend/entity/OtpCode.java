package com.serviprox.serviprox_backend.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "otp_codes")
public class OtpCode {

    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;

    // Téléphone destinataire
    @Column(nullable = false)
    private String telephone;

    // Code OTP à 6 chiffres hashé
    @Column(nullable = false)
    private String code;

    // ID de l'acteur concerné
    @Column(nullable = false)
    private Long acteurId;

    // Date d'expiration → 2 minutes
    @Column(nullable = false)
    private LocalDateTime expireAt;

    // true = déjà utilisé
    @Column(nullable = false)
    private boolean utilise = false;

    // Date de création
    @Column(nullable = false)
    private LocalDateTime createdAt =
            LocalDateTime.now();
}
