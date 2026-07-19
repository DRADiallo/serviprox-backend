package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PreuveRealisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreuveRealisationService {
    private final PreuveRealisationRepository preuveRealisationRepository;
    private final CommandeRepository commandeRepository;
    private final NotificationService notificationService;

    /**
     * Ajouter une preuve de réalisation
     *
     * Contexte sénégalais — 3 types de preuves :
     *
     * PHOTO → Pressing : photos articles collectés/livrés
     *         Soins    : photo du travail effectué
     *         Réparation : photo avant/après réparation
     *
     * VIDEO → Réparation si client absent :
     *         Technicien filme l'appareil réparé
     *         qui fonctionne correctement
     *         Envoyée via WhatsApp ou l'application
     *
     * SIGNATURE → Signature numérique du client
     *             sur l'écran du téléphone de l'agent
     *             Remplace le bon papier souvent perdu
     */

    public PreuveRealisation ajouterPreuve(Long commandeId,String typePreuve,String contenuURL,Long agentId){

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));


        // Creeons la preuve
        PreuveRealisation preuve = new PreuveRealisation();
        preuve.setCommande(commande);

        //// Type : PHOTO, VIDEO ou SIGNATURE
        preuve.setType(TypePreuve.valueOf(typePreuve));

       // URL
        preuve.setContenu(contenuURL);

        // Horodatage — crucial pour les litiges pour la photo
        preuve.setDate(LocalDateTime.now());

        PreuveRealisation saved = preuveRealisationRepository.save(preuve);

        // Notifier le client
        // Il peut voir la preuve dans l'application
        notificationService.envoyer(commande.getClient().getId(),"PREUVE_AJOUTEE","📸 Une preuve a été ajoutée pour "+"votre commande "+commandeId+"Vérifiez et validez");

        return saved;
    }

    /**
     * Comparer bon de collecte et bon de livraison
     *
     * Innovation principale du pressing ServiProx !
     *
     * Problème réel à Dakar :
     * "J'ai donné 5 chemises, on m'en rend 4"
     * "Mon bazin est revenu avec une tache"
     *
     * Solution :
     * 1. À la collecte : agent photographie chaque article
     *    → Bon de collecte avec photos + signature client
     *
     * 2. À la livraison : agent photographie chaque article
     *    → Bon de livraison avec photos + signature client
     *
     * 3. Système compare les deux bons automatiquement
     *    → Si écart détecté : litige automatique
     *    → Si identiques : commande clôturée
     */

    public boolean comparerBons(Long commandeId , String urlBonCollecte, String urlBonLivraison){
        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Command introuvable "));

        // Dans un système complet : Analyse IA des métadonnées des photos
        // (nombre d'articles, couleurs, état...)
        // Version simplifiée actuelle :  Comparaison des URLs des bons générés

        boolean shamesIdentiques= urlBonCollecte.equals(urlBonLivraison);

        if (!shamesIdentiques){
            // Écart détecté → alerter le prestataire
            notificationService.envoyer(commande.getPrestataire().getId(),"ECART_DETECTE","⚠️ Écart détecté entre le bon " + "de collecte et le bon de livraison "
                    + "pour la commande #" + commandeId  + ". Vérifiez immédiatement.");

            // Alerter aussi le client
            notificationService.envoyer(commande.getClient().getId(),"ECART_DETECTE", "⚠️ Un écart a été détecté dans " + "votre commande #" + commandeId + " Un litige a été ouvert");
        }
        return  shamesIdentiques;
    }

    /**
     * Récupérer la preuve d'une commande
     *
     * Flutter affiche la preuve au client
     * pour qu'il puisse valider ou contester
     */
    public Optional<PreuveRealisation> getByCommande(Long commandeId){
        return preuveRealisationRepository.findByCommandeId(commandeId);
    }
}
