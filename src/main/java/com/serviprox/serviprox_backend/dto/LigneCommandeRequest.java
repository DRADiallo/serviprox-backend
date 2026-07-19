package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class LigneCommandeRequest {
    private  String typePrestation;
    private Double  quantite;
    private String  unite;
    private String  prixUnitaire;
    private String  instruction;
}
