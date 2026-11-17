package com.serviprox.serviprox_backend.dto;

import lombok.Data;

/**
 * DTO pour activer/désactiver collecte & livraison
 * sur la config d'un prestataire existant.
 *
 * Exemple body JSON :
 * {
 *   "avecAgent": true,
 *   "modesDepotAcceptes": "[\"Collecte domicile\",\"Dépôt atelier (livraison)\"]",
 *   "etapesPersonnalisees": "[\"collecte\",\"traitement\",\"livraison\"]"
 * }
 */
@Data
public class ConfigurationCollecteRequest {

    // true  = pressing avec agents collecte/livraison
    // false = client dépose directement au pressing
    private Boolean avecAgent;

    // JSON array des modes acceptés par ce prestataire
    // null → tous les modes de ConfigurationService acceptés
    private String modesDepotAcceptes;

    // JSON array des étapes personnalisées
    // null → utilise ConfigurationService.etapes
    private String etapesPersonnalisees;
}
