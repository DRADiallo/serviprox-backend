package com.serviprox.serviprox_backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class Admin extends Acteur {
    // Pas de champs supplémentaires
    // Tous les champs sont dans acteurs (id, nom, email, etc.)
}
