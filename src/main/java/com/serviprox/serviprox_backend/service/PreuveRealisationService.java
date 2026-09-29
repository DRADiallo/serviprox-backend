package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.PreuveRealisationDTO;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.entity.PreuveRealisation;
import com.serviprox.serviprox_backend.enums.StatutPreuve;
import com.serviprox.serviprox_backend.enums.TypePreuve;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PreuveRealisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public PreuveRealisationDTO ajouterPreuve(
            Long commandeId,
            String typePreuve,
            String photoBase64,
            Long acteurId,
            String statut,
            String commentaire) {

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        TypePreuve type =
                TypePreuve.valueOf(typePreuve);

        PreuveRealisation preuve =
                new PreuveRealisation();
        preuve.setCommande(commande);
        preuve.setType(type);
        preuve.setContenu(photoBase64);
        preuve.setActeurId(acteurId);
        preuve.setStatut(
                statut != null
                        ? StatutPreuve.valueOf(statut)
                        : StatutPreuve.OK);
        preuve.setCommentaire(commentaire);

        return toDTO(preuveRealisationRepository.save(preuve));
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

    public List<PreuveRealisationDTO> getByCommande(
            Long commandeId) {
        return preuveRealisationRepository
                .findByCommandeId(commandeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
    public PreuveRealisationDTO toDTO(
            PreuveRealisation pR) {

        PreuveRealisationDTO dto =
                new PreuveRealisationDTO();
        dto.setId(pR.getId());
        dto.setType(pR.getType() != null
                ? pR.getType().name() : null);
        dto.setContenu(pR.getContenu());
        dto.setDate(pR.getDate());
        if (pR.getCommande() != null)
            dto.setCommandeId(
                    pR.getCommande().getId());

        // ← AJOUT : nouveaux champs
        dto.setActeurId(pR.getActeurId());
        dto.setStatut(pR.getStatut() != null
                ? pR.getStatut().name() : "OK");
        dto.setCommentaire(pR.getCommentaire());

        return dto;
    }


}



















