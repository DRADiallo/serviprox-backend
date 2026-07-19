package com.serviprox.serviprox_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "configurations_service")
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String typeService; // pressing, reparation, soins

    @Column
    private String labelAffiche;

    @Column(columnDefinition = "TEXT")
    private String etapes; // JSON

    @Column(columnDefinition = "TEXT")
    private String champsCommande; // JSON

    @Column(columnDefinition = "TEXT")
    private String typesPrestation; // JSON

    @Column(columnDefinition = "TEXT")
    private String optionsDisponibles; // JSON

    @Column
    private Boolean exigeDevis = false;

    @Column
    private Boolean exigePreuve = true;

    @Column
    private Boolean actif = true;
}
