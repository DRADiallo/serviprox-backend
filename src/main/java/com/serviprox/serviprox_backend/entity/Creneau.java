package com.serviprox.serviprox_backend.entity;

import com.serviprox.serviprox_backend.enums.StatutCreneau;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "creneaux")
@NoArgsConstructor
@AllArgsConstructor
public class Creneau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    private StatutCreneau statut = StatutCreneau.LIBRE;

}
