package com.serviprox.serviprox_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommandeRequest {
    private Long prestataireId;
    private String modeDepot;
    private String typeService;
    private List<LigneCommandeRequest>lignes;
}
