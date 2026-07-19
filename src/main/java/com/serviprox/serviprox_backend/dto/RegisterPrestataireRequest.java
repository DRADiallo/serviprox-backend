package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class RegisterPrestataireRequest {
    private String nom;
    private String prenom;
    private String nomCommercial;
    private String email;
    private String telephone;
    private String zoneCouverture;
    private String typeService;
    private String motDePasse;
}
