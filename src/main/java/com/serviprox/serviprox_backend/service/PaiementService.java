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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final NotificationService notificationService;

    // ═══ CREATE ═══
    // Créer un paiement après validation
    // de la prestation par le client
    //
    // Contexte sénégalais — 3 modes :
    // ESPECES      → paiement cash (très fréquent)
    // MOBILE_MONEY → Wave, Orange Money (populaire)
    // CARTE        → moins fréquent

    public PaiementDTO creerPaiement(Long commandeId, Double montant, String modePaiement) {

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande Introuvable"));

        // Vérifions qu'il n'y a pas déjà
        // un paiement pour cette commande
        paiementRepository
                .findByCommandeId(commandeId)
                .ifPresent(p -> {
                    throw new RuntimeException("Un paiement existe déjà " + "pour cette commande");

                });
        // Vérifier mode paiement valide
        ModePaiement mode;
        try {
            mode = ModePaiement.valueOf(modePaiement);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Mode de paiement invalide:" + modePaiement);
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(montant);
        paiement.setMode(mode);
        // EN_ATTENTE → paiement initié
        // pas encore confirmé

        paiement.setStatut(StatutPaiement.EN_ATTENTE);

        return toDTO(paiementRepository.save(paiement));
    }
    // ═══ READ ALL ═══
    public List<PaiementDTO>getAll(){

        return paiementRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY ID ═══
    public PaiementDTO gettById(Long id){

        return toDTO(paiementRepository
                .findById(id)
                .orElseThrow(()->new RuntimeException("Paiement Introuvable")));

    }

    // ═══ READ BY COMMANDE ═══
    public PaiementDTO gettByCommande(Long paiementId){

        Paiement paiement= paiementRepository
                .findById(paiementId)
                .orElseThrow(()->new RuntimeException("Paiement Introuvable"));
        paiement.setStatut(StatutPaiement.EFFECTUE);
        paiement.setDatePaiement(LocalDateTime.now());
        Paiement saved = paiementRepository.save(paiement);

        // Clôturer la commande
        var commande = paiement.getCommande();
        commande.setStatutCommande(StatutCommande.CLOTUREE);
        commandeRepository.save(commande);

        // Notifier le prestataire
        // Argent libéré !
        notificationService.envoyer(
                commande.getPrestataire().getId(),
                "PAIEMENT_CONFIRME",
                "Paiement de"
                + paiement.getCommande()
                +"FCFA confirmé "
                +" - Commande # "+ commande.getId()

        );
        //Notifier le Client

        notificationService.envoyer(
                commande.getClient().getId(),
                "COMMANDE_CLOTUREE",
                "Votre commande " + commande.getId()
                        +" est clôturée. Merci !"
        );

        return toDTO(saved);
    }

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
        dto.setDatePaiement(p.getDatePaiement());
        if (p.getCommande() != null)
            dto.setCommandeId(
                    p.getCommande().getId());
        return dto;
    }

}


