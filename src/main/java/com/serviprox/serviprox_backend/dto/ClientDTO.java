package com.serviprox.serviprox_backend.dto;


import lombok.Data;

/**
 * DTO Client — évite d'exposer le mot de passe
 * et les données sensibles de l'entité
 */
@Data
public class ClientDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String statut;
}
