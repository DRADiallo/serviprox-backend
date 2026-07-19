package com.serviprox.serviprox_backend.entity;

import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="acteurs")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="type_acteur")
@NoArgsConstructor
@AllArgsConstructor
public class Acteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutActeur statut = StatutActeur.ACTIF;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

}
