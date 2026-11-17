package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.PaiementDTO;
import com.serviprox.serviprox_backend.entity.Paiement;
import com.serviprox.serviprox_backend.enums.ModePaiement;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.enums.StatutPaiement;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final NotificationService notificationService;
    private final FcmService fcmService;


    // ═══ CREATE ═══
    // Créer un paiement après validation
    // de la prestation par le client
    //
    // Contexte sénégalais — 3 modes :
    // ESPECES      → paiement cash (très fréquent)
    // MOBILE_MONEY → Wave, Orange Money , Free (populaire)


    // ← FIX 1 : creerPaiement
    // Ajouter capture + référence
    @Transactional
    public PaiementDTO creerPaiement(Long commandeId, Double montant, String modePaiement,
            String captureBase64,
            String reference) {

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        paiementRepository
                .findByCommandeId(commandeId)
                .ifPresent(p -> {
                    throw new RuntimeException(
                            "Paiement déjà existant !");
                });

        ModePaiement mode;
        try {
            mode = ModePaiement.valueOf(modePaiement);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Mode invalide: " + modePaiement);
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(montant);
        paiement.setMode(mode);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        // ← AJOUT
        paiement.setCaptureEcranBase64(captureBase64);
        paiement.setReferenceTransaction(reference);
        // ← Client confirme avoir payé
        paiement.setConfirmeClient(true);

        Paiement saved =
                paiementRepository.save(paiement);

        fcmService.envoyerNotification(
                commande.getPrestataire().getId(),
                "💰 Paiement reçu !",
                "Le client a payé "
                        + montant + " FCFA via "
                        + modePaiement
                        + " — Commande #" + commandeId
                        + ". Veuillez confirmer !",
                "PAIEMENT_RECU");

        // ← Passer statut PAIEMENT_RECU
        commande.setStatutCommande(StatutCommande.EN_COURS);
        commande.setEtapeCourante("livraison");
        commandeRepository.save(commande);

        // ← Notifier prestataire
        notificationService.envoyer(
                commande.getPrestataire().getId(),
                "PAIEMENT_RECU",
                "Paiement de " + montant
                        + " FCFA reçu via " + modePaiement
                        + " — Commande #" + commandeId
                        + ". Veuillez confirmer la réception !");

        return toDTO(saved);
    }

    // ← FIX 2 : getByCommande
    // findByCommandeId au lieu de findById !
    public PaiementDTO getByCommande(Long commandeId) {
        return toDTO(paiementRepository
                .findByCommandeId(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Paiement introuvable")));
    }

    // ← FIX 3 : confirmerPaiement (Prestataire)
    // Passe EN_LIVRAISON pas CLOTUREE !
    @Transactional
    public PaiementDTO confirmerPaiement(Long id) {

        Paiement paiement = paiementRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Paiement introuvable"));

        // ← Prestataire confirme
        paiement.setConfirmePrestataire(true);
        paiement.setStatut(StatutPaiement.EFFECTUE);
        paiement.setDatePaiement(
                LocalDateTime.now());
        paiement.setDateConfirmation(
                LocalDateTime.now());

        Paiement saved = paiementRepository.save(paiement);
        fcmService.envoyerNotification(
                paiement.getCommande().getClient().getId(),
                "✅ Paiement confirmé !",
                "Votre paiement a été confirmé "
                        + "par le prestataire. "
                        + "Commande #"
                        + paiement.getCommande().getId(),
                "PAIEMENT_CONFIRME");



        // ← Passer EN_LIVRAISON
        // (pas CLOTUREE directement !)
        var commande = paiement.getCommande();
        commande.setStatutCommande(StatutCommande.EN_COURS);
        commande.setEtapeCourante("livraison");
        commandeRepository.save(commande);

        // ← Notifier client
        notificationService.envoyer(
                commande.getClient().getId(),
                "PAIEMENT_CONFIRME",
                "Votre paiement a été confirmé ! "
                        + "Votre commande #"
                        + commande.getId()
                        + " est en cours de livraison.");

        // ← Notifier prestataire
        notificationService.envoyer(
                commande.getPrestataire().getId(),
                "PAIEMENT_CONFIRME",
                "Paiement commande #"
                        + commande.getId()
                        + " confirmé ! Assignez un agent "
                        + "pour la livraison.");

        return toDTO(saved);
    }

    /////////////////////////////////////////NEW /////////////////////////////////////////////
    // ═══ READ ALL ═══
    public List<PaiementDTO>getAll(){

        return paiementRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }




    // ═══ READ BY ID ═══
    public PaiementDTO getById(Long id){

        return toDTO(paiementRepository
                .findById(id)
                .orElseThrow(()->new RuntimeException("Paiement Introuvable")));

    }

    // ═══ READ BY COMMANDE ═══
//    public PaiementDTO getByCommande(Long commandeId){
//
//        return  toDTO(paiementRepository
//                .findById(commandeId)
//                .orElseThrow(()->new RuntimeException("Paiement Introuvable")));
//
//    }
    // ═══ CONFIRMER PAIEMENT ═══
    // Séparée de getByCommande()

//    public PaiementDTO confirmerPaiement(Long id){
//
//        Paiement paiement= paiementRepository
//                .findById(id)
//                .orElseThrow(()-> new RuntimeException("Paiement Introuvable"));
//        paiement.setStatut(StatutPaiement.EFFECTUE);
//        paiement.setDateConfirmation(LocalDateTime.now());
//        Paiement saved= paiementRepository.save(paiement);
//
//        // Clôturer la commande
//        var commande = paiement.getCommande();
//        commande.setStatutCommande(StatutCommande.CLOTUREE);
//        commandeRepository.save(commande);
//
//        // Notifier le prestataire
//        // Argent libéré !
//        notificationService.envoyer(
//                commande.getPrestataire().getId(),
//                "PAIEMENT_CONFIRME",
//                "Paiement de"
//                        + paiement.getCommande()
//                        +"FCFA confirmé "
//                        +" - Commande # "+ commande.getId()
//
//        );
//        //Notifier le Client
//
//        notificationService.envoyer(
//                commande.getClient().getId(),
//                "COMMANDE_CLOTUREE",
//                "Votre commande " + commande.getId()
//                        +" est clôturée. Merci !"
//        );
//
//        return toDTO(saved);
//    }

    // ═══ REMBOURSER ═══
    // En cas de litige résolu
    // en faveur du client

    public PaiementDTO rembourser(Long paiementId){

        Paiement paiement= paiementRepository
                .findById(paiementId)
                .orElseThrow(()->new RuntimeException("Paiement Introuvable"));
        paiement.setStatut(StatutPaiement.REMBOURSE);
        Paiement saved = paiementRepository.save(paiement);

        //Notifier le Client
        notificationService.envoyer(
                paiement.getCommande().getClient().getId(),
                "REMBOURSEMENT",
                "Remboursement de"
                        + paiement.getMontant()
                        +"FCFA en cours "
                        +" - Commande # "+ paiement.getCommande().getId()

        );
        return  toDTO(saved);
    }

    // ═══ MAPPER entité → DTO ═══
    public PaiementDTO toDTO(Paiement p) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(p.getId());
        dto.setMontant(p.getMontant());
        dto.setMode(p.getMode() != null
                ? p.getMode().name() : null);
        dto.setStatut(p.getStatut() != null
                ? p.getStatut().name() : null);
        dto.setDatePaiement(p.getDateConfirmation());
        if (p.getCommande() != null)
            dto.setCommandeId(
                    p.getCommande().getId());
        return dto;
    }

}


