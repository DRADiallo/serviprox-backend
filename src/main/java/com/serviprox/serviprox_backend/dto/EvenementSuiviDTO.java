package com.serviprox.serviprox_backend.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO EvenementSuivi — historique commande
 * Timeline visible par client et admin React
 */
@Data
public class EvenementSuiviDTO {
    private Long id;
    private Long acteurId;
    private Long commandeId;
    private String type;
    private String ancienStatut;
    private String nouveauStatut;
    private String message;
    private LocalDateTime dateEvenement;
}
