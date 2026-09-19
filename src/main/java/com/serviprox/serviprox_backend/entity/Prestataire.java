package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "prestataires")
@DiscriminatorValue("PRESTATAIRE")
@AllArgsConstructor
public class Prestataire extends Acteur{
    @Column
    private String nomCommercial;

    @Column
    private double noteMoyenne=0.0;

    @Column
    private String zoneCouverture;

    // ← AJOUT : type de service proposé
    // Ex: "Pressing", "Réparation", "Soins domicile"
    // Correspond au typeService dans ConfigurationService
    @Column
    private String typeService;

    // Commandes reçues
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL)
    private List<Commande> commandes;

    // AgentsLivreurs  du prestataire
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL)
    private List<Agent> agents;

    // Configuration personnalisée
    //  EAGER charge la config automatiquement
    @OneToOne(mappedBy = "prestataire", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private ConfigPrestataire configPrestataire;

    public Prestataire() {
        setRole(RoleUtilisateur.PRESTATAIRE);
        setStatut(StatutActeur.EN_ATTENTE_VALIDATION);
    }


}
