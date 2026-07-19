package com.serviprox.serviprox_backend.dto;

import lombok.Data;

@Data
public class RegisterClientRequest {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adressePrincipale;
    private String motDePasse;

}
