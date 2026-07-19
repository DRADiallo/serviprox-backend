package com.serviprox.serviprox_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "evenements_suivi")
@NoArgsConstructor
@AllArgsConstructor
public class EvenementSuivi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String type;

    @Column
    private String ancienStatut;

    @Column
    private String nouveauStatut;

    @Column
    private LocalDateTime dateEvenement = LocalDateTime.now();

    @Column
    private Long acteurId;

    @Column(columnDefinition = "TEXT")
    private String message; // "Litige signalé par client : article manquant"

    @ManyToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;
}
