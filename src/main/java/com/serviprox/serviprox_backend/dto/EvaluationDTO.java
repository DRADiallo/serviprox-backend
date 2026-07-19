package com.serviprox.serviprox_backend.dto;

import lombok.Data;

/**
 * DTO Evaluation — note et feedback client
 */
@Data
public class EvaluationDTO {
    private Long id;
    private Long clientId;
    private String nomClient;
    private Long prestataireId;
    private String nomPrestataire;
    private Integer note;
    private String commentaire;
}
