package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class ConfigurationServiceDTO {
    private Long id;
    private String typeService;
    private String labelAffiche;
    private String etapes;
    private String champsCommande;
    private String typesPrestation;
    private String optionsDisponibles;
    private Boolean exigeDevis;
    private Boolean exigePreuve;
    private Boolean actif;
}
