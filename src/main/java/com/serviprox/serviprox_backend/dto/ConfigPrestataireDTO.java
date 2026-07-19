package com.serviprox.serviprox_backend.dto;

import lombok.Data;

/**
 * DTO ConfigPrestataire — configuration métier
 */
@Data
public class ConfigPrestataireDTO {
    private Long id;
    private Long prestataireId;
    private String nomPrestataire;
    private String typeService;
    private String tarifsPersonnalises;
    private String optionsActives;
    private String prestationsAjoutees;
    private String zoneCouverture;
}
