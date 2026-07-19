package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.entity.Intervention;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.InterventionRepository;
import com.serviprox.serviprox_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private  final InterventionRepository interventionRepository;
    private  final CommandeRepository commandeRepository;
    private  final NotificationService notificationService;

    /**
     * Démarrons une intervention
     *
     * Contexte sénégalais :
     *
     * Pressing   → 2 interventions par commande
     *   Intervention 1 : Collecte du linge
     *   Intervention 2 : Livraison du linge traité
     *
     * Réparation → 1 intervention
     *   Intervention 1 : Visite technicien
     *
     * Soins      → 1 intervention
     *   Intervention 1 : Prestation sur place
     */
    public Intervention demarrerIntervention(Long commandeId, String adresseDepart,
                                             String adresseArrivee,String typeIntervention ){

        // COLLECTE, LIVRAISON, REPARATION, SOIN

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));

        // Create Intervention
        Intervention intervention= new Intervention();
        intervention.setCommande(commande);
        intervention.setAdresseDepart(adresseDepart);
        intervention.setAdresseArrivee(adresseArrivee);
        intervention.setTypeIntervention(typeIntervention);

        // heure de debut
        intervention.setDateDebut(LocalDateTime.now());

        // Mettre à jour le statut de la commande
        // CONFIRMEE → EN_TRAITEMENT
        commande.setStatutCommande(StatutCommande.EN_TRAITEMENT);
        commandeRepository.save(commande);

        Intervention saved= interventionRepository.save(intervention);

        // Notifier le client que l'agent est en route

        notificationService.envoyer(commande.getClient().getId(),"AGENT_EN_ROUTE","Votre agent est en route ! "
                + "Type : " + typeIntervention);
        return  saved;
    }

    /**
     * Terminer une intervention
     *
     * L'agent signale la fin de sa mission
     * → Heure de fin enregistrée
     * → Statut commande mis à jour
     *
     * Pressing collecte terminée → EN_LIVRAISON
     * Pressing livraison terminée → TERMINEE
     * Réparation terminée → TERMINEE
     * Soins terminés → TERMINEE
     */

    public Intervention terminerIntervention(Long interventionId,String typeIntervention){

        Intervention intervention = interventionRepository
                .findById(interventionId)
                .orElseThrow(()->new RuntimeException("Intervention introuvable"));

        // save l heure de fin

        intervention.setDateFin(LocalDateTime.now());
        interventionRepository.save(intervention);

        Commande commande = intervention.getCommande();

        // Logique selon le type d intervention
        if ("COLLECTE".equals(typeIntervention)){
            // Pressing : collecte terminée
            // → passer en EN_LIVRAISON pour le traitement
            commande.setStatutCommande(StatutCommande.EN_LIVRAISON);

            notificationService.envoyer(commande.getClient().getId(),"COLLECTE_TERMINEE",
                    "✅ Collecte terminée ! "
                            + "Votre linge est en cours de traitement."
            );

        }else {
            // Livraison, Réparation ou Soins terminés
            // → TERMINEE, en attente validation client
            commande.setStatutCommande(StatutCommande.TERMINEE);

            notificationService.envoyer(commande.getClient().getId(),"INTERVENTION_TERMINEE",
                    "✅ Prestation terminée ! "
                            + "Veuillez valider et évaluer le service."
            );

        }
        commandeRepository.save(commande);
        return intervention;
    }

    /**
     * Mettre à jour la position GPS de l'agent
     *
     * Contexte sénégalais — Soins à domicile :
     * Le client voit en temps réel où est
     * l'intervenant sur la carte Flutter
     *
     * - Client peut guider l'agent
     */
    public Intervention mettreAjourPositionGPS(Long interventionId, String positionGPS){

        Intervention intervention = interventionRepository
                .findById(interventionId)
                .orElseThrow(()->new RuntimeException("Intervention inttrouvable "));
         // Update  Position GPS
        intervention.setPositionActuelle(positionGPS);
        return interventionRepository.save(intervention);
    }

    /**
     * Toutes les interventions d'une commande
     *
     * Pour le pressing : retourne 2 interventions
     * (collecte + livraison)
     * Pour les autres : retourne 1 intervention
     */

    public List<Intervention> getByCommande(Long commandeId){
        return  interventionRepository.findByCommandeId(commandeId);
    }


}
