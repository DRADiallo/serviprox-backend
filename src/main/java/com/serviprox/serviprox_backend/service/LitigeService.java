package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LitigeService {

    private  final CommandeRepository commandeRepository;
    private  final EvenementSuiviService evenementSuiviService;
    private final NotificationService notificationService;
    private final CommandeService commandeService;

    /**
     * Client signale un litige
     *
     * 1. Pressing : article manquant ou abîmé
     *    "Mon boubou n'est pas revenu"
     *    "Le bazin est décoloré"
     *
     * 2. Réparation : appareil non réparé
     *    "Mon téléphone ne marche toujours pas"
     *    "Le technicien a gardé mes pièces"
     *
     * 3. Soins : prestation incomplète
     *    "Le ménage n'a pas été bien fait"
     *    "L'intervenante est partie trop tôt"
     */

    public Commande signalerLitige(Long commandeId, String motif, Long clientId){

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));
        // Verfion que c est bien le client correspondant de cette commande
        if (!commande.getClient().getId().equals(clientId)){
            throw  new RuntimeException("Vous n'êtes pas autorisé à signaler \"\n" +
                    "                + \"un litige pour cette commande");
        }

        // Un litige n'est possible que si la prestation est terminée On ne peut pas contester avant la fin !

        if (commande.getStatutCommande()!= StatutCommande.TERMINEE){
            throw new RuntimeException("Litige impossible : "
                    + "la commande n'est pas encore terminée"+
                    commande.getLignesCommande());
        }

        // Sauvegarder l'ancien statut pour l'historique
        String ancienStatut= commande.getStatutCommande().name();

        // Passons la commande en litige
        commande.setStatutCommande(StatutCommande.LITIGE);
        commandeRepository.save(commande);

        // Enregistrer dans l'historique
        evenementSuiviService.enregistrer(commandeId,ancienStatut,"LITTIGE","LITIGE_OUVERT","Litige signalé par le client : "+ motif,clientId);

        // Notifier le prestataire immédiatement
        notificationService.envoyer(commande.getPrestataire().getId(),
                "LITIGE_OUVERT","Litige ouvert pour la commande"+commandeId
        +"\nMotif : "+motif + "\nVeuillez examiner les preuves " + "et contacter le client.");

    return commande;
    }

    // ═══ ARBITRER UN LITIGE ═══
    // Prestataire prend une décision
    //
    // RESOLU   → Litige clarifi
    //            Commande CLOTUREE
    //            Paiement libéré
    //
    // REMBOURSE → Prestataire reconnaît le problème
    //             Commande ANNULEE
    //             Client remboursé
    public Commande arbitrerLitige(Long commandeId,String decision,Long prestataireID){
        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));

        // Vérifier que c'est bien en litige
        if (commande.getStatutCommande() != StatutCommande.LITIGE){
            throw new RuntimeException("Cette commande n'est pas en litige"+
                    " — Statut actuel : "+ commande.getStatutCommande());

        }

        // Vérifier que c'est bien le prestataire
        // de cette commande qui arbitre
        if (!commande.getPrestataire().getId().equals(prestataireID)) {
            throw new RuntimeException("Vous n'êtes pas autorisé " + "à arbitrer ce litige");
        }


        String ancienStatut= "LITIGE";

        if ("RESOLU".equals(decision)){

            // Litige résolu → clôturer la commande

            commande.setStatutCommande(StatutCommande.CLOTUREE);

            evenementSuiviService.enregistrer(commandeId,ancienStatut,"CLOTUREE","LITIGE_CLOTURE","Litige résolu par le prestataire",prestataireID);

            // Notifier le client
            notificationService.envoyer(commande.getClient().getId(),"LITIGE_RESOLU","Votre litige pour la commande"+ commandeId +"a été résolu. Merci de votre confiance");

        } else if ("REMBOURSE".equals(decision)){

            // Remboursement → annuler la commande

            commande.setStatutCommande(StatutCommande.ANNULEE);

            evenementSuiviService.enregistrer(commandeId,ancienStatut,"ANNULEE","REMBOURSEMENT","Remboursement accordé suite au litige",prestataireID);

            // Notifier le client
            notificationService.envoyer(commande.getClient().getId(),"REMBOURSEMENT","Remboursement accordé pour " + "La commande" + commandeId + "Vous serez contacte sous 24h"
                    );
        }else {
            throw  new RuntimeException("Decision invalide , utilisez resolu ou Rembourse");
        }
        commandeRepository.save(commande);
        return  commande;
    }

    // ═══ READ — TOUS LES LITIGES ═══
    // Admin React — supervision globale
    public List<CommandeDTO> getAllLitiges() {
        return commandeRepository
                .findByStatutCommande(
                        StatutCommande.LITIGE)
                .stream()
                .map(commandeService::toDTO)
                .collect(Collectors.toList());

    }
    // ═══ READ — LITIGES D'UN CLIENT ═══
    // Historique des litiges du client

    public List<CommandeDTO> getLitigesClient( Long clientId) {
        return commandeRepository
                .findByClientId(clientId)
                .stream()
                .filter(c -> c.getStatutCommande() == StatutCommande.LITIGE)
                .map(commandeService::toDTO)
                .collect(Collectors.toList());
    }

}
