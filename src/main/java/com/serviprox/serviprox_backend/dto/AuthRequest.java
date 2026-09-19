package com.serviprox.serviprox_backend.dto;


import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private  String motDePasse;
    // AJOUT : téléphone pour vérification
    // et envoi OTP
    private String telephone;
}
