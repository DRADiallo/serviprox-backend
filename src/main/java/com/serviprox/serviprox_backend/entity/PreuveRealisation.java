package com.serviprox.serviprox_backend.entity;


import com.serviprox.serviprox_backend.enums.TypePreuve;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "preuves_realisation")
@NoArgsConstructor
@AllArgsConstructor
public class PreuveRealisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypePreuve type;

    @Column(columnDefinition = "TEXT")
    private String contenu; // URL

    @Column
    private LocalDateTime date = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "commande_id")
    private Commande commande;
}
