package com.serviprox.serviprox_backend.entity;

import com.serviprox.serviprox_backend.enums.ModePaiement;
import com.serviprox.serviprox_backend.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "paiements")
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    private ModePaiement mode;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    @Column
    private LocalDateTime datePaiement;

    // ← AJOUT : capture Wave/Orange/Free
    @Column(columnDefinition = "TEXT")
    private String captureEcranBase64;

    // ← AJOUT : référence transaction
    @Column
    private String referenceTransaction;

    // ← AJOUT : confirmation double
    // Client confirme avoir payé
    @Column
    private Boolean confirmeClient = false;

    // Prestataire confirme avoir reçu
    @Column
    private Boolean confirmePrestataire = false;

    @Column
    private LocalDateTime  dateConfirmation;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;

}
