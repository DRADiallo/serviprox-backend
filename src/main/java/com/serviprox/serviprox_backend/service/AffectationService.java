package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.AffectationDTO;
import com.serviprox.serviprox_backend.entity.Affectation;
import com.serviprox.serviprox_backend.entity.Agent;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.AffectationRepository;
import com.serviprox.serviprox_backend.repository.AgentRepository;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class AffectationService{
    private final AffectationRepository affectationRepository;
    private final AgentRepository agentRepository;
    private final CommandeRepository commandeRepository;
    private  final  NotificationService notificationService;

    // ═══ CREATE — Affecter un agent à une commande ═══

    // Contexte sénégalais :
    // Pressing   → livreur disponible choisi
    //              par le prestataire
    // Réparation → technicien selon compétences
    // Soins      → intervenant selon créneau

    public AffectationDTO affecterAgent( Long commandeId, Long agentId){

        // Recupoerer la commande

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande Introuvable"));

        // Recuperer L agent
        Agent agent= agentRepository
                .findById(agentId)
                .orElseThrow(()-> new RuntimeException(" Agent introuvable"));

        // Vérifier que l'agent est disponible
        // Un agent en mission ne peut pas
        // recevoir une nouvelle affectation

        if (!agent.getDisponibilite()){
            throw new RuntimeException("Agent indisponible -!! "+ "en cours de mission");

        }

        // Creer  l affection
        Affectation affectation= new Affectation();
        affectation.setCommande(commande);
        affectation.setAgent(agent);
        affectation.setDateAffectation(LocalDateTime.now());
        // Staut initial de l affectation
        affectation.setStatut("EN_ATTENTE");

        // Agent → non disponible pendant la mission
        // Sera remis disponible après fin de prestation
        agent.setDisponibilite(false);
        agentRepository.save(agent);

        // Commande -> CREE A CONFIRMEE
        commande.setStatutCommande(StatutCommande.CONFIRMEE);
        commandeRepository.save(commande);

        // Sauvegarder l affectation
        Affectation saved= affectationRepository.save(affectation);

        // Notifier l'agent
        // Au Sénégal : agents souvent sur le terrain
        // la notification push est essentielle

        notificationService.envoyer(
                agentId,
                "NOUVELLE_MISSION",
                "Nouvelle mission !"
                + "Commande"+commandeId
                +"!! Rendez-vous Chez le Client "
        );

        //NOtifier Le Cliennt

        notificationService.envoyer(
                commande.getClient().getId(),
                "COMMANDE_CONFIRMEE",
                "Votre Commande:"+commandeId
                + "est confirmée ! "
                + "Un agent vous été assigné"
        );
        return toDTO(saved);

    }
    // ═══ READ BY ID ═══
    public AffectationDTO getById(Long id){

        return toDTO(affectationRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Aucune Affectation" + "pour cette commande")));

    }
    // ═══ READ BY AGENT ═══
    // Historique de toutes les missions
    // d'un agent — utile pour ses performances

    public List<AffectationDTO> getByAgent(Long agentId){

        return affectationRepository
                .findByAgentId(agentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());


    }

    // ═══ UPDATE STATUT AFFECTATION ═══
    // Statuts possibles :
    // EN_ATTENTE → agent notifié, pas encore parti
    // EN_COURS   → agent en déplacement
    // TERMINEE   → mission accomplie

    public AffectationDTO actualiserStatu(Long id, String statut){
        Affectation affectation= affectationRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Affectation Introuvable"));

        affectation.setStatut(statut);
        return toDTO(affectationRepository.save(affectation));

    }

    // ═══ LIBÉRER UN AGENT ═══
    // Appelé quand la prestation est terminée
    // L'agent redevient disponible
    // pour une nouvelle mission

    public void libererAgent(Long agentId){

        Agent agent= agentRepository
                .findById(agentId)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));


        // Remettre l'agent disponible
        agent.setDisponibilite(true);

        // Notifier l argent
        notificationService.envoyer(
                agentId,
                "MISSION_TERMINEE",
                "Mission terminée ! "
                + "Vous êtes à nouveau disponible.! "
        );

    }

    // ═══ DELETE ═══
    // Annuler une affectation
    // Ex: agent indisponible après affectation
    // → on réaffecte un autre agent

    public void annulerAffectation(Long id){

        Affectation affectation= affectationRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Affectation Introuvable"));


        // Remettre l'agent disponible
        // avant de supprimer l'affectation
        Agent agent = affectation.getAgent();
        agent.setDisponibilite(true);
        agentRepository.save(agent);

        affectationRepository.deleteById(id);
    }

    // ═══ MAPPER entité → DTO ═══
    public AffectationDTO toDTO(Affectation a) {
        AffectationDTO dto = new AffectationDTO();
        dto.setId(a.getId());
        dto.setDateAffectation(a.getDateAffectation());
        dto.setOrdrePassage(a.getOrdrePassage());
        dto.setZoneGeographique(a.getZoneGeographique());
        // On expose uniquement les IDs
        // pas les objets complets
        // pour éviter les boucles infinies
        if (a.getAgent() != null) {
            dto.setAgentId(a.getAgent().getId());
            dto.setNomAgent(
                    a.getAgent().getNom()
                            + " " + a.getAgent().getPrenom());
        }
        if (a.getCommande() != null)
            dto.setCommandeId(
                    a.getCommande().getId());

        return dto;
    }

}








//
//import com.serviprox.serviprox_backend.entity.Affectation;
//import com.serviprox.serviprox_backend.entity.Agent;
//import com.serviprox.serviprox_backend.entity.Commande;
//import com.serviprox.serviprox_backend.enums.StatutCommande;
//import com.serviprox.serviprox_backend.repository.AffectationRepository;
//import com.serviprox.serviprox_backend.repository.AgentRepository;
//import com.serviprox.serviprox_backend.repository.CommandeRepository;
//import com.serviprox.serviprox_backend.repository.NotificationRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AffectationService {
//    private  final AffectationRepository affectationRepository;
//    private  final AgentRepository agentRepository;
//    private  final CommandeRepository commandeRepository;
//    private  final NotificationService notificationService;
//
//    /**
//     * Affecter un agent à une commande
//     *
//     * Contexte sénégalais :
//     * - Pressing : prestataire choisit son livreur disponible
//     * - Réparation : technicien selon compétences
//     * - Soins : intervenant selon disponibilité et créneau
//     */
//
//    public Affectation affecterAgent(Long commandeId, Long agentId){
//
//        // Récupérons la commande — erreur si inexistante
//        Commande commande= commandeRepository
//                .findById(commandeId)
//                .orElseThrow(()->new RuntimeException("Commande introuvable"));
//
//        // Récupérons l'agent
//        Agent agent= agentRepository
//                .findById(agentId)
//                .orElseThrow(()->new RuntimeException("Agent introuvable"));
//
//        // Vérifions que l'agent est disponible
//        // Un agent occupé ne peut pas recevoir une nouvelle mission
//        if (!agent.getDisponibilite()){
//            throw new RuntimeException("Agent insdisponible");
//        }
//        // Créeons  l'affectation
//        Affectation affectation= new Affectation();
//        affectation.setCommande(commande);
//        affectation.setAgent(agent);
//        affectation.setDateAffectation(LocalDateTime.now());
//
//        // Marquons l'agent comme non disponible
//        // Il sera remis disponible après la prestation
//        agent.setDisponibilite(false);
//        agentRepository.save(agent);
//
//        // Mettre à jour le statut de la commande
//        // CREEE → CONFIRMEE après affectation agent
//        commande.setStatutCommande(StatutCommande.CONFIRMEE);
//        commandeRepository.save(commande);
//
//        // Sauvegardons l'affectation en base
//        Affectation saved= affectationRepository.save(affectation);
//
//        // Notifions  l'agent de sa nouvelle mission
//        // Important au Sénégal : SMS/push notification
//        // car les agents sont souvent sur le terrain
//        notificationService.envoyer(agentId, "NOUVELLE_MISSION", "Nouvelle mission assignée !"
//        +"Commande:"+ commandeId + "Rendez-vous chez le client");
//
//        // Notifions le client que sa commande est confirmée
//        notificationService.envoyer(commande.getClient().getId(),"COMMANDE_CONFIRMEE","\"✅ Votre commande:"+commandeId
//        + "est confirmée !"+ "Un agent vous a été assigné.");
//        return saved;
//    }
//
//    /**
//     * Libérer un agent après sa mission
//     *
//     * Appelé quand la prestation est terminée
//     * → Agent redevient disponible pour
//     *   une nouvelle mission
//     */
//    public void libererAgent(Long agentId){
//        Agent agent = agentRepository
//                .findById(agentId)
//                .orElseThrow(()->new RuntimeException("Agent introuvable"));
//
//        // Remmettons l agent disponible
//        agent.setDisponibilite(true);
//        agentRepository.save(agent);
//    }
//
//    /**
//     * Récupérons l'affectation d'une commande
//     *
//     * Utilisé par Flutter pour savoir quel agent
//     * est assigné à une commande donnée
//     */
//
//    public Affectation getByCommande(Long commandeId){
//        return  affectationRepository
//                .findByCommandeId(commandeId)
//                .orElseThrow(()->new RuntimeException("Aucune Affectation pour cette commande"));
//
//    }
//
//    /**
//     * Toutes les affectations d'un agent
//     *
//     * Historique des missions de l'agent
//     * Utile pour calculer ses performances
//     */
//
//    public List<Affectation>getByAgent(Long agentId){
//        return  affectationRepository.findByAgentId(agentId);
//    }
//}
