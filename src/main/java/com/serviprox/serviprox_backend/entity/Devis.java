package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.StatutDevis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "devis")
@NoArgsConstructor
@AllArgsConstructor
public class Devis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montantEstime;

    @Enumerated(EnumType.STRING)
    private StatutDevis statut = StatutDevis.EN_ATTENTE;

    @Column
    private LocalDateTime dateGeneration = LocalDateTime.now();

    @Column
    private LocalDateTime dateValidation;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;
}
