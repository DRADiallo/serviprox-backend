package com.serviprox.serviprox_backend.entity;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statutCommande = StatutCommande.CREE;

    // ─── MODE DE DÉPÔT ──────────────────────────────────────────────────
    // Valeur libre choisie par le client depuis champsCommande JSON (Couche 2)
    // Ex : "Collecte domicile", "Dépôt atelier (livraison)", etc.
    @Column
    private String modeDepot;

    // ─── ÉTAPE COURANTE DU WORKFLOW ──────────────────────────────────────
    // Étape métier en cours, lue depuis ConfigurationService.etapes (Couche 2)
    // null → commande pas encore EN_COURS (CREE ou CONFIRMEE)
    //
    // pressing   : "collecte" | "traitement" | "livraison"
    // reparation : "diagnostic" | "reparation" | "restitution"
    // soins      : "reservation" | "intervention" | "validation"
    //
    // Flutter l'affiche avec le label humain correspondant.
    // Le prestataire/agent avance l'étape via avancerEtape().
    @Column
    private String etapeCourante;


    // ─── ÉTAPES EFFECTIVES AU MOMENT DE LA COMMANDE ─────────────────────
    // Snapshot des étapes configurées par le prestataire lors de la création
    // Ex: ["CREE","CONFIRMEE","reception","traitement","restitution","CLOTUREE"]
    // Permet d'afficher les étapes historiques même si le prestataire les modifie plus tard
    @Column(columnDefinition = "TEXT")
    private String etapesEffectives;


    // Données de configuration en JSON
    @Column(columnDefinition = "TEXT")
    private String configData;

    // Saisie par le client lors de la commande
    @Column(columnDefinition = "TEXT")
    private String descriptionColis;

    @Column
    private Double prixParTypePrestations = 0.0;

    // Prix course collecte
    @Column
    private Double prixCourseCollecte = 0.0;

    // Prix course livraison
    @Column
    private Double prixCourseLivraison = 0.0;

    // Client a accepté le prix réel
    @Column
    private Boolean prixAccepteClient = false;

    // Détail prix réel en JSON
    @Column(columnDefinition = "TEXT")
    private String detailPrixReel;

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

    // Affectation Livreur (2 possibles : collecte + livraison)
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Affectation> affectations;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private VerificationCollecte verificationCollecte;
}