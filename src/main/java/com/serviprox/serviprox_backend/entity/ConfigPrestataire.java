package com.serviprox.serviprox_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "config_prestataires")
@NoArgsConstructor
@AllArgsConstructor
public class ConfigPrestataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String tarifsPersonnalises; // JSON

    @Column(columnDefinition = "TEXT")
    private String optionsActives; // JSON

    @Column(columnDefinition = "TEXT")
    private String prestationsAjoutees; // JSON

    @Column(columnDefinition = "TEXT")
    private String zoneCouverture; // JSON

    // Lié à un seul prestataire
    @OneToOne
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;

    // Instancie une ConfigurationService
    @ManyToOne
    @JoinColumn(name = "configuration_service_id")
    private ConfigurationService configurationService;

}
