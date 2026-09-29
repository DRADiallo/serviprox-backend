package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.StatutPreuve;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "preuves_realisation")
@NoArgsConstructor
@AllArgsConstructor
public class PreuveRealisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypePreuve type;

    //  AVANT : contenu (URL)
    //  APRÈS : photoBase64 + URL
    @Column(columnDefinition = "TEXT")
    private String contenu; // Base64 ou URL


    //  AJOUT : statut
    // OK → tout bon
    // PROBLEME → article endommagé/manquant
    @Enumerated(EnumType.STRING)
    @Column
    private StatutPreuve statut = StatutPreuve.OK;

    //  AJOUT : commentaire si problème
    @Column
    private String commentaire;

    //  AJOUT : qui a créé la preuve
    @Column
    private Long acteurId;


    @Column
    private LocalDateTime date = LocalDateTime.now();


    // OneToMany: car plusieurs preuves par commande
    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

}
