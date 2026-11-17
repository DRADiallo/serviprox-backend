package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.AffectationDTO;
import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.entity.Affectation;
import com.serviprox.serviprox_backend.entity.Agent;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.AffectationRepository;
import com.serviprox.serviprox_backend.repository.AgentRepository;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FcmService fcmService;

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

    // ═══ READ BY COMMANDE ═══
    // Une commande → une seule affectation
    // Utilisé par Flutter pour savoir
    // quel agent est assigné à la commande

    public AffectationDTO getByCommande(Long commandeId){


        List<Affectation> affectations = affectationRepository
                        .findByCommandeId(commandeId);

        if (affectations.isEmpty()) {
            throw new RuntimeException(
                    "Aucune affectation pour "
                            + "cette commande #" + commandeId);
        }

        return toDTO(affectations.get(0));
    }




//        return toDTO(affectationRepository.findByCommandeId(commandeId)
//                .orElseThrow(()-> new RuntimeException("Aucune affectation pour cette commande #"+commandeId)));


    // ═══ UPDATE STATUT AFFECTATION ═══
    // Statuts possibles :
    // EN_ATTENTE → agent notifié, pas encore parti
    // EN_COURS   → agent en déplacement
    // TERMINEE   → mission accomplie

    public AffectationDTO actualiserStatut(Long id, String statut){
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
        agentRepository.save(agent);

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

    // ← AJOUT : Agent confirme ou refuse mission
    @Transactional
    public AffectationDTO confirmerMission(Long commandeId, Long agentId, String decision) {

        // Trouver affectation
        Affectation affectation =
                affectationRepository
                        .findByCommandeIdAndAgentId(
                                commandeId, agentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Mission introuvable"));

        Commande commande =
                commandeRepository
                        .findById(commandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Commande introuvable"));

        if ("CONFIRMER".equals(decision)) {
            // ← Agent accepte
            affectation.setStatut("CONFIRMEE");
            affectationRepository.save(affectation);

            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "✅ Mission acceptée !",
                    affectation.getAgent().getPrenom()
                            + " a accepté la mission #"
                            + commandeId
                            + ". En route pour la collecte !",
                    "MISSION_ACCEPTEE");


            // Notifier client aussi
            fcmService.envoyerNotification(
                    commande.getClient().getId(),
                    "🛵 Agent en route !",
                    affectation.getAgent().getPrenom()
                            + " est en route pour "
                            + "collecter vos articles !",
                    "AGENT_EN_ROUTE");

            // ← Notifier prestataire
            notificationService.envoyer(
                    commande.getPrestataire().getId(),
                    "MISSION_CONFIRMEE",
                    "L'agent " +
                            affectation.getAgent().getPrenom()
                            + " a confirmé la mission #"
                            + commandeId);

        } else if ("REFUSER".equals(decision)) {
            // ← Agent refuse
            affectation.setStatut("REFUSEE");
            affectationRepository.save(affectation);

            // ← Libérer l'agent
            affectation.getAgent()
                    .setDisponibilite(true);
            agentRepository.save(
                    affectation.getAgent());

            // Si REFUSER :
            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "⚠️ Mission refusée !",
                    affectation.getAgent().getPrenom()
                            + " a refusé la mission #"
                            + commandeId
                            + ". Assignez un autre agent !",
                    "MISSION_REFUSEE");

            // ← Notifier prestataire
            notificationService.envoyer(
                    commande.getPrestataire().getId(),
                    "MISSION_REFUSEE",
                    "L'agent " +
                            affectation.getAgent().getPrenom()
                            + " a refusé la mission #"
                            + commandeId
                            + ". Assignez un autre agent !");
        }

        return toDTO(affectation);
    }
    // ← Missions d'un agent
    public List<AffectationDTO> getMissionsAgent(
            Long agentId) {
        return affectationRepository
                .findByAgentId(agentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    // ← Missions en attente
    public List<AffectationDTO> getMissionsEnAttente(
            Long agentId) {
        return affectationRepository
                .findByAgentIdAndStatut(
                        agentId, "EN_ATTENTE")
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // ═══ MAPPER entité → DTO ═══
    public AffectationDTO toDTO(Affectation a) {
        AffectationDTO dto = new AffectationDTO();
        dto.setId(a.getId());
        dto.setStatut(a.getStatut());
        dto.setOrdrePassage(a.getOrdrePassage());
        dto.setZoneGeographique(
                a.getZoneGeographique());
        dto.setDateAffectation(
                a.getDateAffectation());
        dto.setTypeAffectation(
                a.getTypeAffectation());

        // ← Agent
        if (a.getAgent() != null) {
            dto.setAgentId(a.getAgent().getId());
            dto.setNomAgent(
                    a.getAgent().getPrenom()
                            + " " + a.getAgent().getNom());
        }

        // ← Commande
        if (a.getCommande() != null) {
            dto.setCommandeId(
                    a.getCommande().getId());
            dto.setMontantTotal(
                    a.getCommande().getMontantTotal());
            dto.setMotDepot(a.getCommande().getModeDepot());
            dto.setDescriptionColis(
                    a.getCommande().getDescriptionColis());

            // Client
            if (a.getCommande().getClient() != null) {
                dto.setNomClient(
                        a.getCommande().getClient()
                                .getPrenom()
                                + " "
                                + a.getCommande().getClient()
                                .getNom());
                dto.setTelephoneClient(
                        a.getCommande().getClient()
                                .getTelephone());
            }

            // Prestataire
            if (a.getCommande().getPrestataire()
                    != null) {
                dto.setNomPrestataire(
                        a.getCommande().getPrestataire()
                                .getNomCommercial());
            }
        }

        return dto;
    }

}