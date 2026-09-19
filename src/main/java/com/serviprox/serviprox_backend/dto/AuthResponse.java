package com.serviprox.serviprox_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
}
