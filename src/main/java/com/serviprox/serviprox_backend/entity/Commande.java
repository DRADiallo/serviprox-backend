package com.serviprox.serviprox_backend.entity;

import com.serviprox.serviprox_backend.enums.ModeDepot;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "commandes")
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Double montantTotal = 0.0;

    @Column(nullable = false)
    private LocalDateTime dateCommande = LocalDateTime.now();

    // ← CHAMP STATUT AJOUTÉ
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statutCommande = StatutCommande.CREE;

    // Mode de dépôt choisi
    @Enumerated(EnumType.STRING)
    private ModeDepot modeDepot;

    // Données de configuration en JSON
    @Column(columnDefinition = "TEXT")
    private String configData;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Prestataire qui reçoit la commande
    @ManyToOne
    @JoinColumn(name = "prestataire_id", nullable = false)
    private Prestataire prestataire;

    // Lignes de commande
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignesCommande;

    // Interventions liées
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Intervention> interventions;

    // Paiement
    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Paiement paiement;

    // Evenements de suivi
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<EvenementSuivi> evenementsSuivi;

    // Preuve de réalisation
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<PreuveRealisation> preuves;

    // Devis (optionnel)
    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Devis devis;

    // Affectation Livreur
    //OneToMany
    // car 2 affectations possibles (collecte + livraison)
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Affectation> affectations;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private VerificationCollecte verificationCollecte;
}
