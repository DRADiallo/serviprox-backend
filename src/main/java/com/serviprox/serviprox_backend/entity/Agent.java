package com.serviprox.serviprox_backend.entity;

import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="Agent")
//@DiscriminatorColumn(name="LIVREURS")
@AllArgsConstructor
public class Agent extends Acteur{
    @Column
    private String motoMatricule;

    @Column
    private String competences;

    @Column
    private Boolean disponibilite = true;

    @Column
    private String positionActuelle;
    // AJOUT : code PIN pour connexion
    // Généré automatiquement par le prestataire
    // Envoyé par SMS à l'agent
    @Column
    private String codePin;


    // Prestataire auquel l'agent appartient
    @ManyToOne
    @JoinColumn(name = "prestataire_id")
    private Prestataire prestataire;

    // Affectations de l'agent
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Affectation> affectations;

    public Agent() {
        setRole(RoleUtilisateur.LIVREUR);
    }
}

