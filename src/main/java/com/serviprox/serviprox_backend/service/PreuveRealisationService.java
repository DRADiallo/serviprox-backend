package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.PreuveRealisationDTO;
import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PreuveRealisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class PreuveRealisationService{

    private final PreuveRealisationRepository preuveRealisationRepository;
    private final CommandeRepository commandeRepository;
    private final NotificationService notificationService;

    // ═══ CREATE — Ajouter une preuve ═══
    // Contexte sénégalais — 3 types de preuves :
    //
    // PHOTO → Pressing : photos articles collectés/livrés
    //         Soins    : photo du travail effectué
    //         Réparation : photo avant/après réparation
    //
    // VIDEO → Réparation si client absent :
    //         Technicien filme l'appareil réparé
    //         Envoyée via l'application
    //
    // SIGNATURE → Signature numérique du client
    //             sur l'écran du téléphone de l'agent
    //             Remplace le bon papier souvent perdu

    public PreuveRealisationDTO ajouterPreuve( Long commandeId, String typePreuve,String contenuUrl, Long agentId){

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande Introuvable"));

        // Vérifier que le type est valide
        // PHOTO, VIDEO ou SIGNATURE

        TypePreuve type;
        try {
            type = TypePreuve.valueOf(typePreuve);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de preuve invalide :"+ typePreuve+ "veuillez utiliser PHOTO, VIDEOou SIGNATURE");

        }

        PreuveRealisation preuve= new PreuveRealisation();
        preuve.setCommande(commande);
        preuve.setType(type);
        preuve.setContenu(contenuUrl);
        preuve.setDate(LocalDateTime.now());

        PreuveRealisation saved = preuveRealisationRepository.save(preuve);

        notificationService.envoyer(
                commande.getClient().getId(),
                "PREUVE_AJOUTEE",
                "Une preuve a été ajoutée"
                + "pour votre commande #:"+ commandeId
                + "Veuillez verifier et valider ! "
        );
         return toDTO(saved);
    }

        // ═══ READ BY ID ═══
        public  PreuveRealisationDTO getById(Long id){

            return toDTO(preuveRealisationRepository
                    .findById(id)
                    .orElseThrow(()-> new RuntimeException("Preuve Introuvable")));

        }

        // ═══ READ BY COMMANDE ═══
        // Flutter affiche la preuve au client
        // pour qu'il puisse valider ou contester

        public  PreuveRealisationDTO getByCommande(Long commandeId){

        return toDTO(preuveRealisationRepository
                .findByCommandeId(commandeId)
                .orElseThrow(()-> new RuntimeException("Aucune preuve pour"+ "cette commande")));
        }

        // ═══ READ ALL ═══
        // Utilisé par AdminService pour supervision

        public List<PreuveRealisationDTO> getAll(){

        return preuveRealisationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        }

    // ═══ COMPARER BONS ═══
    // Innovation principale du pressing ServiProx !
    //
    // Problème réel à Dakar :
    // "J'ai donné 5 chemises, on m'en rend 4"
    // "Mon bazin est revenu avec une tache"
    //
    // Solution double preuve :
    // 1. Collecte : agent photographie chaque article
    //    → Bon de collecte + signature client
    // 2. Livraison : agent photographie chaque article
    //    → Bon de livraison + signature client
    // 3. Système compare automatiquement
    //    → Écart détecté : litige automatique
    //    → Identiques : commande clôturée

    public boolean comparerBons(Long commandeId, String urlBonCollecte, String urlBonLivraison){

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande Introuvable"));

        // Version actuelle simplifiée :
        // Comparaison des URLs des bons générés
        //
        // Version future avec IA :
        // Analyse métadonnées photos
        // (nombre articles, couleurs, état...)

        boolean sontIdentiques= urlBonCollecte.equals(urlBonLivraison);

        if (!sontIdentiques){

            notificationService.envoyer(
                    // Écart détecté → alerter prestataire
                    commande.getPrestataire().getId(),
                    " Écart détecté entre le bon"
                    +    "de collecte et le bon de livraison"
                    +   "Commande" + commandeId
                    +   "de collecte et le bon de livraison",
                    "si vous detecter un ecart fait signal"
            );
            // Alerter le client
            notificationService.envoyer(
                    commande.getClient().getId(),
                    "ECART_DETECTE",
                    "Un écart a été détecté"
                    + "dans votre commande"+ commandeId
                    + "Un litige a été ouvert "
            );


        }
        return  sontIdentiques;
    }

    // ═══ DELETE ═══
    // Supprimer une preuve
    // Uniquement par l'admin en cas d'erreur
    // Les preuves sont des éléments légaux !
    public  void supprimerPreuve(Long id ){

        if (!preuveRealisationRepository.existsById(id)){
            throw new RuntimeException("Preuve Introuvable");
        }
        preuveRealisationRepository.deleteById(id);
    }

    // ═══ MAPPER entité → DTO ═══
    public PreuveRealisationDTO toDTO(PreuveRealisation pR){

        PreuveRealisationDTO dto= new PreuveRealisationDTO();
        dto.setId(pR.getId());
        dto.setType(pR.getType() !=null? pR.getType().name():null);
        dto.setContenu(pR.getContenu());
        dto.setDate(pR.getDate());
        if (pR.getCommande() !=null)
            dto.setCommandeId(pR.getCommande().getId());
       return dto;
    }


}


















//import com.serviprox.serviprox_backend.entity.PreuveRealisation;
//import com.serviprox.serviprox_backend.enums.TypePreuve;
//import com.serviprox.serviprox_backend.repository.CommandeRepository;
//import com.serviprox.serviprox_backend.repository.PreuveRealisationRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class PreuveRealisationService {
//    private final PreuveRealisationRepository preuveRealisationRepository;
//    private final CommandeRepository commandeRepository;
//    private final NotificationService notificationService;
//
//    /**
//     * Ajouter une preuve de réalisation
//     *
//     * Contexte sénégalais — 3 types de preuves :
//     *
//     * PHOTO → Pressing : photos articles collectés/livrés
//     *         Soins    : photo du travail effectué
//     *         Réparation : photo avant/après réparation
//     *
//     * VIDEO → Réparation si client absent :
//     *         Technicien filme l'appareil réparé
//     *         qui fonctionne correctement
//     *         Envoyée via WhatsApp ou l'application
//     *
//     * SIGNATURE → Signature numérique du client
//     *             sur l'écran du téléphone de l'agent
//     *             Remplace le bon papier souvent perdu
//     */
//
//    public PreuveRealisation ajouterPreuve(Long commandeId,String typePreuve,String contenuURL,Long agentId){
//
//        var commande = commandeRepository
//                .findById(commandeId)
//                .orElseThrow(()-> new RuntimeException("Commande introuvable"));
//
//
//        // Creeons la preuve
//        PreuveRealisation preuve = new PreuveRealisation();
//        preuve.setCommande(commande);
//
//        //// Type : PHOTO, VIDEO ou SIGNATURE
//        preuve.setType(TypePreuve.valueOf(typePreuve));
//
//       // URL
//        preuve.setContenu(contenuURL);
//
//        // Horodatage — crucial pour les litiges pour la photo
//        preuve.setDate(LocalDateTime.now());
//
//        PreuveRealisation saved = preuveRealisationRepository.save(preuve);
//
//        // Notifier le client
//        // Il peut voir la preuve dans l'application
//        notificationService.envoyer(commande.getClient().getId(),"PREUVE_AJOUTEE","📸 Une preuve a été ajoutée pour "+"votre commande "+commandeId+"Vérifiez et validez");
//
//        return saved;
//    }
//
//    /**
//     * Comparer bon de collecte et bon de livraison
//     *
//     * Innovation principale du pressing ServiProx !
//     *
//     * Problème réel à Dakar :
//     * "J'ai donné 5 chemises, on m'en rend 4"
//     * "Mon bazin est revenu avec une tache"
//     *
//     * Solution :
//     * 1. À la collecte : agent photographie chaque article
//     *    → Bon de collecte avec photos + signature client
//     *
//     * 2. À la livraison : agent photographie chaque article
//     *    → Bon de livraison avec photos + signature client
//     *
//     * 3. Système compare les deux bons automatiquement
//     *    → Si écart détecté : litige automatique
//     *    → Si identiques : commande clôturée
//     */
//
//    public boolean comparerBons(Long commandeId , String urlBonCollecte, String urlBonLivraison){
//        var commande = commandeRepository
//                .findById(commandeId)
//                .orElseThrow(()-> new RuntimeException("Command introuvable "));
//
//        // Dans un système complet : Analyse IA des métadonnées des photos
//        // (nombre d'articles, couleurs, état...)
//        // Version simplifiée actuelle :  Comparaison des URLs des bons générés
//
//        boolean shamesIdentiques= urlBonCollecte.equals(urlBonLivraison);
//
//        if (!shamesIdentiques){
//            // Écart détecté → alerter le prestataire
//            notificationService.envoyer(commande.getPrestataire().getId(),"ECART_DETECTE","⚠️ Écart détecté entre le bon " + "de collecte et le bon de livraison "
//                    + "pour la commande #" + commandeId  + ". Vérifiez immédiatement.");
//
//            // Alerter aussi le client
//            notificationService.envoyer(commande.getClient().getId(),"ECART_DETECTE", "⚠️ Un écart a été détecté dans " + "votre commande #" + commandeId + " Un litige a été ouvert");
//        }
//        return  shamesIdentiques;
//    }
//
//    /**
//     * Récupérer la preuve d'une commande
//     *
//     * Flutter affiche la preuve au client
//     * pour qu'il puisse valider ou contester
//     */
//    public Optional<PreuveRealisation> getByCommande(Long commandeId){
//        return preuveRealisationRepository.findByCommandeId(commandeId);
//    }
//}
