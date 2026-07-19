package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Notification — message système
 */
@Data
public class NotificationDTO {
    private Long id;
    private Long destinataire;
    private String type;
    private String message;
    private String statut;
    private LocalDateTime dateEnvoi;
}
