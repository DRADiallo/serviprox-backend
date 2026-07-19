package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.StatutNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long destinataire;

    @Column
    private String type;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatutNotification statut = StatutNotification.NON_LU;

}
