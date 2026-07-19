package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Table(name = "clients")
@DiscriminatorValue("CLIENT")
@AllArgsConstructor
public class Client extends  Acteur {
    @Column
    private String adressePrincipale;

    @Column
    private String coordonneesGPS;

    // Un client passe plusieurs commandes
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Commande> commandes;

    // Un client laisse plusieurs évaluations
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Evaluation> evaluations;

    public Client() {
        setRole(RoleUtilisateur.CLIENT);
    }

}
