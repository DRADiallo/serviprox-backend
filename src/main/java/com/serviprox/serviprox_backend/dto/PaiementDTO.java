package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Paiement — transaction financière
 */
@Data
public class PaiementDTO {
    private Long id;
    private Long commandeId;
    private Double montant;
    private String mode;
    private String statut;
    private LocalDateTime datePaiement;
}
