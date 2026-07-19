package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "lignes_commande")
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typePrestation;

    @Column(nullable = false)
    private Double quantite;

    @Column
    private String unite; // KG ou PIECE

    @Column(nullable = false)
    private Double prixUnitaire;

    @Column
    private Double montantLigne;

    @Column
    private String instructions;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @PrePersist
    @PreUpdate
    public void calculerMontant() {
        if (quantite != null && prixUnitaire != null) {
            this.montantLigne = quantite * prixUnitaire;
        }
    }
}
