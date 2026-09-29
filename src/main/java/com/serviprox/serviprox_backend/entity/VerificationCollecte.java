package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;



@Entity
@Data
@Table(name = "verifications_collecte")
public class VerificationCollecte {

    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

    // ← Prestataire confirme réception
    @Column
    private Boolean confirmePrestataire = false;

    @Column
    private String commentairePrestataire;

    // ← Client confirme ses articles
    @Column
    private Boolean confirmeClient = false;

    @Column
    private String commentaireClient;

    // ← Statut global
    // EN_ATTENTE → VALIDE → LITIGE
    @Column
    private String statut = "EN_ATTENTE";

    @Column
    private LocalDateTime dateCreation = LocalDateTime.now();
}
