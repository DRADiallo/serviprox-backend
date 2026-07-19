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

    // Commandes reçues
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL)
    private List<Commande> commandes;

    // AgentsLivreurs  du prestataire
    @OneToMany(mappedBy = "prestataire", cascade = CascadeType.ALL)
    private List<Agent> agents;

    // Configuration personnalisée
    @OneToOne(mappedBy = "prestataire", cascade = CascadeType.ALL)
    private ConfigPrestataire configPrestataire;

    public Prestataire() {
        setRole(RoleUtilisateur.PRESTATAIRE);
        setStatut(StatutActeur.EN_ATTENTE_VALIDATION);
    }


}
