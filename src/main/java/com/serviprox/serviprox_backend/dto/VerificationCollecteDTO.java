package com.serviprox.serviprox_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VerificationCollecteDTO {
    private Long id;
    private Long commandeId;
    private Boolean confirmePrestataire;
    private String commentairePrestataire;
    private Boolean confirmeClient;
    private String commentaireClient;
    private String statut;
    private LocalDateTime dateCreation;
}
