package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.DevisDTO;
import com.serviprox.serviprox_backend.entity.Devis;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.enums.StatutDevis;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.DevisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DevisService {
    private final DevisRepository devisRepository;
    private final CommandeRepository commandeRepository;
    private final NotificationService notifService;

    // ═══ CREATE ═══
    // Technicien génère un devis
    // après diagnostic de l'appareil
    //
    // Contexte sénégalais :
    // OBLIGATOIRE avant toute réparation
    // Client doit valider avant intervention

    public DevisDTO creerDevis(Long commandeId, Double montantEstime){

        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande  intoruvable"));

        // Vérifier qu'il n'y a pas déjà un devis
        // Une commande = un seul devis
        devisRepository
                .findByCommandeId(commandeId)
                .ifPresent(d -> {
                    throw new RuntimeException("Un devis existe déjà "+ "pour cette commande");
                });
        Devis devis = new Devis();
        devis.setCommande(commande);
        devis.setMontantEstime(montantEstime);
        devis.setStatut(StatutDevis.EN_ATTENTE);
        devis.setDateGeneration(LocalDateTime.now());

        Devis saved = devisRepository.save(devis);

        // Notifier le client
        // Il doit valider ou refuser le devis
         notifService.envoyer(
                 commande.getClient().getId(),
                 "DEVIS_RECU",
                 "Vous avez reçu un devis de :"
                 + montantEstime + "FCFA"
                 + "pour votre commande " + commandeId
                 + "Veuillez valider ou refuser !"
         );
         return toDTO(saved);

    }

    // ═══ READ BY ID ═══
    public DevisDTO getById(Long id){

        return toDTO(devisRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Devis Introuvable")));




    }
    // ═══ READ BY COMMANDE  ═══

    public DevisDTO getByCommande(Long commandeId){

        return toDTO(devisRepository
                .findByCommandeId(commandeId)
                .orElseThrow(()-> new RuntimeException("Aucun devis pour"+ "cette commande")));

    }

    // ═══ VALIDER ═══
    // Client accepte le devis
    // → Réparation peut commencer
    // → Commande passe EN_ATTENTE_PIECES
    public DevisDTO validerDevis(Long devisId){

        Devis devis= devisRepository
                .findById(devisId)
                .orElseThrow(()-> new RuntimeException("Devis Introuvable"));

        // Vérifier que le devis est en attente
        if ( devis.getStatut() != StatutDevis.EN_ATTENTE){
            throw new RuntimeException("Ce devis a déjà été traité:"+ devis.getStatut());

        }
        devis.setStatut(StatutDevis.VALIDE);
        devis.setDateValidation(LocalDateTime.now());
        Devis saved = devisRepository.save(devis);

        // Notifier le prestataire
        notifService.envoyer(
                devis.getCommande().getPrestataire().getId(),
                "DEVIS_VALIDE",
                "Le client a validé le devis"
                + "de"+ devis.getMontantEstime()
                + "FCFA- Commande"
                + devis.getCommande().getId()
                +"Procédez à la réparation !!"
        );

        return toDTO(saved);
    }

    // ═══ REFUSER ═══
    // Client refuse le devis
    // → Commande annulée automatiquement
    // → Aucune réparation sans accord client

    public DevisDTO refuserDevis(Long devisId) {

        Devis devis = devisRepository
                .findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis Introuvable"));

        if (devis.getStatut() != StatutDevis.EN_ATTENTE) {
            throw new RuntimeException("Ce devis a déjà été traité:" + devis.getStatut());

        }

        devis.setStatut(StatutDevis.REFUSE);
        Devis saved = devisRepository.save(devis);

        // Annuler la commande
        var commande = devis.getCommande();
        commande.setStatutCommande(StatutCommande.ANNULEE);
        commandeRepository.save(commande);

        // Notifier le prestataire
        notifService.envoyer(
                devis.getCommande().getPrestataire().getId(),
                "DEVIS_REFUSE",
                "Le client a refusé le devis"

                        + "- Commande#" + commande.getId()
                        + "Annulée !!"
        );
        return toDTO(saved);
    }

        // ═══ MAPPER entité → DTO ═══
        public DevisDTO toDTO(Devis d){

        DevisDTO dto= new DevisDTO();
        dto.setId(d.getId());
        dto.setMontantEstime(d.getMontantEstime());
        dto.setStatut(d.toString() !=null? d.getStatut().name():null);
        dto.setDateGeneration(d.getDateGeneration());
        dto.setDateValidation(d.getDateValidation());
        if (d.getCommande() !=null)
            dto.setCommandeId(d.getCommande().getId());
        return dto;

        }

}
