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

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;


}
