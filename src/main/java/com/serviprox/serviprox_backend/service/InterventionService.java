package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.InterventionDTO;
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
import java.util.stream.Collectors;

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
     *   Intervention 1 : Visite technicien ou depot en atelier
     *
     * Soins      → 1 intervention
     *   Intervention 1 : Prestation sur place
     */
    public InterventionDTO demarrerIntervention(Long commandeId, String adresseDepart,
                                             String adresseArrivee,String typeIntervention ){

        // COLLECTE, LIVRAISON, REPARATION, SOIN

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));

        /** Vérifier que la commande est CONFIRMEE
        * On ne peut pas démarrer une intervention
        * sur une commande non confirmée
        * Create Intervention

         */
        if (commande.getStatutCommande() != StatutCommande.CONFIRMEE
                && commande.getStatutCommande() !=StatutCommande.EN_LIVRAISON){
            throw new RuntimeException("Impossible de démarrer : "
                                        + "commande non confirmée. "
                                        + "Statut : "
                                        + commande.getStatutCommande());

        }

        // Creons l' intervention

        Intervention intervention= new Intervention();
        intervention.setCommande(commande);
        intervention.setAdresseDepart(adresseDepart);
        intervention.setAdresseArrivee(adresseArrivee);
        intervention.setTypeIntervention(typeIntervention);

        // heure de debut
        intervention.setDateDebut(LocalDateTime.now());
        intervention.setStatut("EN_COURS");

        // Mettre à jour le statut de la commande
        // CONFIRMEE → EN_TRAITEMENT
        commande.setStatutCommande(StatutCommande.EN_TRAITEMENT);
        commandeRepository.save(commande);

        Intervention saved= interventionRepository.save(intervention);

        // Notifier le client que l'agent est en route

        notificationService.envoyer(commande.getClient().getId(),"AGENT_EN_ROUTE",
                                        "Votre agent est en route ! "
                                        + "Type : " + typeIntervention
                                        +  " — Suivez sa position en temps réel !");
        return  toDTO(saved);

    }

    // ═══ READ BY ID ═══
    public InterventionDTO getById(Long id) {
        return toDTO(interventionRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Intervention introuvable")));
    }

    // ═══ READ BY COMMANDE ═══
    // Pressing → retourne 2 interventions
    //            (collecte + livraison)
    // Autres   → retourne 1 intervention

    /**
     * Toutes les interventions d'une commande
     *
     * Pour le pressing : retourne 2 interventions
     * (collecte + livraison)
     * Pour les autres : retourne 1 intervention
     */

    public List<InterventionDTO> getByCommande(Long commandeId) {

        return interventionRepository
                .findByCommandeId(commandeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ ALL ═══
    // Admin React — supervision
    public List<InterventionDTO> getAll() {
        return interventionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    public InterventionDTO terminerIntervention(Long interventionId,String typeIntervention){

        Intervention intervention = interventionRepository
                .findById(interventionId)
                .orElseThrow(()->new RuntimeException("Intervention introuvable"));

        // save l heure de fin

        intervention.setDateFin(LocalDateTime.now());
        intervention.setStatut("TERMINEE");
        interventionRepository.save(intervention);

        Commande commande = intervention.getCommande();

        // Logique selon le type d intervention

        if ("COLLECTE".equals(typeIntervention)) {
            // ← Collecte terminée
            // → EN_VERIFICATION (prestataire vérifie)
            // pas EN_LIVRAISON directement !
            commande.setStatutCommande(
                    StatutCommande.EN_VERIFICATION);

            notificationService.envoyer(
                    commande.getClient().getId(),
                    "COLLECTE_TERMINEE",
                    "✅ Collecte terminée ! "
                            + "Le prestataire vérifie "
                            + "vos articles.");

        } else {
            // ← Livraison/Réparation/Soins terminés
            // → VERIFICATION_FINALE
            // pas TERMINEE (supprimé !)
            commande.setStatutCommande(
                    StatutCommande.VERIFICATION_FINALE);

            notificationService.envoyer(
                    commande.getClient().getId(),
                    "INTERVENTION_TERMINEE",
                    "✅ Prestation terminée ! "
                            + "Veuillez vérifier et valider.");
        }
        commandeRepository.save(commande);
        return  toDTO(intervention);
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
    public InterventionDTO mettreAjourPositionGPS(Long interventionId, String positionGPS){

        Intervention intervention = interventionRepository
                .findById(interventionId)
                .orElseThrow(()->new RuntimeException("Intervention inttrouvable "));
         // Update  Position GPS
        intervention.setPositionActuelle(positionGPS);
        return toDTO(interventionRepository.save(intervention));
    }

    // ═══ UPDATE STATUT ═══
    // Changer le statut de l'intervention
    // EN_COURS, TERMINEE, ANNULEE
    public InterventionDTO changerStatut(
            Long id, String statut) {
        Intervention intervention =
                interventionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Intervention introuvable"));

        intervention.setStatut(statut);
        return toDTO(interventionRepository
                .save(intervention));
    }

    // ═══ DELETE ═══
    // Annuler une intervention
    // Uniquement si pas encore démarrée
    public void supprimer(Long id) {
        Intervention intervention =
                interventionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Intervention introuvable"));

        // Ne pas supprimer si en cours
        if ("EN_COURS".equals(
                intervention.getStatut())) {
            throw new RuntimeException(
                    "Impossible de supprimer "
                            + "une intervention en cours");
        }

        interventionRepository.deleteById(id);
    }

    // ═══ MAPPER entité → DTO ═══
    public InterventionDTO toDTO(
            Intervention i) {
        InterventionDTO dto = new InterventionDTO();
        dto.setId(i.getId());
        dto.setAdresseDepart(i.getAdresseDepart());
        dto.setAdresseArrivee(i.getAdresseArrivee());
        dto.setTypeIntervention(
                i.getTypeIntervention());
        dto.setPositionActuelle(
                i.getPositionActuelle());
        dto.setDateDebut(i.getDateDebut());
        dto.setDateFin(i.getDateFin());
        if (i.getCommande() != null)
            dto.setCommandeId(
                    i.getCommande().getId());
        return dto;
    }




    

//    public List<Intervention> getByCommande(Long commandeId){
//        return  interventionRepository.findByCommandeId(commandeId);
//    }


}
