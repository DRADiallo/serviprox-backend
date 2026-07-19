package com.serviprox.serviprox_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "affectations")
@NoArgsConstructor
@AllArgsConstructor
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime dateAffectation = LocalDateTime.now();

    @Column
    private String statut;

    @Column
    private Integer ordrePassage;

    @Column
    private String zoneGeographique;

    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Agent agent;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;
}
