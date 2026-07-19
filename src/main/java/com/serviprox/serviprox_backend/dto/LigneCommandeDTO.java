package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class LigneCommandeDTO {
    private String typePrestation;
    private Double quantite;
    private String unite;
    private Double prixUnitaire;
    private Double montantLigne;

}
