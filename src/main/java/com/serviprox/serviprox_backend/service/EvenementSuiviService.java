package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.entity.EvenementSuivi;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.EvenementSuiviRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvenementSuiviService {
    private final EvenementSuiviRepository evenementSuiviRepository;
    private final CommandeRepository commandeRepository;

    /**
     * Enregistrer un événement de suivi
     *
     * Appelé automatiquement à chaque changement
     * de statut d'une commande
     *
     * Exemple timeline d'une commande pressing :
     * 09h00 - CREEE      → Cliente Awa passe commande
     * 09h05 - CONFIRMEE  → Pressing Médina accepte
     * 10h00 - EN_TRAITEMENT → Agent collecte le linge
     * 10h30 - EN_LIVRAISON → Linge au pressing
     * 14h00 - TERMINEE   → Livraison effectuée
     * 14h05 - CLOTUREE   → Client paie et évalue
     *
     * Cette timeline est visible dans Flutter
     * pour que le client suive sa commande
     */

    public EvenementSuivi enregistrer(Long commandeId,
                                      String ancienStatut,
                                      String nouveauStatut,
                                      String type,
                                      String message,
                                      Long userId){
        Commande commande= commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable "));

        // Creeons L' evemenent
        EvenementSuivi evt= new EvenementSuivi();
        evt.setCommande(commande);
        // Statut avant le changement
        // ex: "CREEE"
        evt.setAncienStatut(ancienStatut);

        // Statut après le changement
        // ex: "CONFIRMEE"
        evt.setNouveauStatut(nouveauStatut);

        evt.setType(type);

        evt.setMessage(message);

        // Qui a déclenché l'événement
        // (clientId, prestataireId ou agentId)
        evt.setActeurId(userId);

        // Horodatage précis de l'événement
        // Crucial pour les litiges :
        // "La collecte a eu lieu à 10h00 le 15/01/2026"
        evt.setDateEvenement(LocalDateTime.now());

        return evenementSuiviRepository.save(evt);
    }

        /**
         * Historique complet d'une commande
         *
         * Flutter affiche cette timeline au client
         * comme un tracking de colis
         *
         * En cas de litige : l'admin verra exactement
         * qui a fait quoi et à quelle heure
         */

        public List<EvenementSuivi> getHistorique(Long commandeId){
            return  evenementSuiviRepository.findByCommandeId(commandeId);
        }



}
