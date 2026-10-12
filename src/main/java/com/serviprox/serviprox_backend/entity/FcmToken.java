package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens")
@Data
public class FcmToken {
    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String role;
    // CLIENT, PRESTATAIRE, AGENT

    @Column(nullable = false,
            columnDefinition = "TEXT")
    private String token;

    @Column
    private LocalDateTime updatedAt =
            LocalDateTime.now();
}
