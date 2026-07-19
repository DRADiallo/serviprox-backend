package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class PrestataireDTO {
    private Long id;
    private String nomCommercial;
    private String adresse;
    private String zone;
    private Double noteMoyenne;
    private String typeService;
    private Boolean ouvert;
    private String configData;
}
