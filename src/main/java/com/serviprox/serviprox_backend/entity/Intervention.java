package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "interventions")
@NoArgsConstructor
@AllArgsConstructor
public class Intervention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String statut;

    @Column
    private String adresseDepart;

    @Column
    private String adresseArrivee;

    @Column
    private String typeIntervention;

    @Column
    private String positionActuelle;

    @Column
    private LocalDateTime dateDebut;

    @Column
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "creneau_id")
    private Creneau creneau;

}
