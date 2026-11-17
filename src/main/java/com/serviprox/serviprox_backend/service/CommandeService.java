package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.CommandeRequest;
import com.serviprox.serviprox_backend.dto.LigneCommandeDTO;
import com.serviprox.serviprox_backend.entity.*;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final PrestataireRepository prestataireRepository;
    private final EvenementSuiviService evenementSuiviService;
    private final AgentRepository agentRepository;
    private final AffectationRepository affectationRepository;
    private final NotificationService notificationService;
    private final FcmService fcmService;

    // ═══════════════════════════════════════════════════════════════
    // CREATE — Client passe une commande
    // ═══════════════════════════════════════════════════════════════
    public CommandeDTO creerCommande(Long clientId, CommandeRequest req) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        Prestataire prestataire = prestataireRepository.findById(req.getPrestataireId())
                .orElseThrow(() -> new RuntimeException("Prestataire introuvable"));

        if (prestataire.getStatut() != StatutActeur.ACTIF) {
            throw new RuntimeException("Ce prestataire n'est pas disponible");
        }

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setPrestataire(prestataire);
        commande.setStatutCommande(StatutCommande.CREE);

        // ✅ modeDepot est maintenant un String (plus d'enum ModeDepot)
        // La valeur vient directement du formulaire Flutter,
        // lu depuis ConfigurationService.champsCommande (Couche 2)
        // Ex: "Collecte domicile", "Dépôt atelier (livraison)"
        if (req.getModeDepot() != null) {
            commande.setModeDepot(req.getModeDepot());
        }

        if (req.getDescriptionColis() != null) {
            commande.setDescriptionColis(req.getDescriptionColis());
        }

        // Lignes de commande
        if (req.getLignes() != null) {
            List<LigneCommande> lignes = req.getLignes()
                    .stream().map(l -> {
                        LigneCommande ligneCommande = new LigneCommande();
                        ligneCommande.setTypePrestation(l.getTypePrestation());
                        ligneCommande.setQuantite(l.getQuantite());
                        ligneCommande.setUnite(l.getUnite());
                        ligneCommande.setPrixUnitaire(Double.valueOf(l.getPrixUnitaire()));
                        ligneCommande.setInstructions(l.getInstruction());
                        ligneCommande.setCommande(commande);
                        return ligneCommande;
                    }).collect(Collectors.toList());
            commande.setLignesCommande(lignes);

            double totalPrix = lignes.stream()
                    .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire())
                    .sum();
            commande.setPrixParTypePrestations(totalPrix);
            commande.setMontantTotal(totalPrix);
        }

        Commande saved = commandeRepository.save(commande);

        fcmService.envoyerNotification(
                saved.getPrestataire().getId(),
                "🔔 Nouvelle commande !",
                "Client " + client.getPrenom() + " " + client.getNom()
                        + " a passé une commande #" + saved.getId(),
                "NOUVELLE_COMMANDE");

        evenementSuiviService.enregistrer(
                saved.getId(), null, "CREE", "CREATION",
                "Commande créée par le client", clientId);

        return toDTO(saved);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ
    // ═══════════════════════════════════════════════════════════════
    public List<CommandeDTO> getAll() {
        return commandeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CommandeDTO getById(Long id) {
        return toDTO(commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable")));
    }

    public List<CommandeDTO> getByClient(Long id) {
        return commandeRepository.findByClientId(id).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getByPrestataire(Long prestataireId) {
        return commandeRepository.findByPrestataireId(prestataireId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getByStatut(StatutCommande statutCommande) {
        return commandeRepository.findByStatutCommande(statutCommande).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTUALISER STATUT — change le statut UNIVERSEL de la commande
    // ═══════════════════════════════════════════════════════════════
    //
    // ✅ Architecture Couche 1 (mémoire) :
    // Cette méthode ne connaît QUE les 6 statuts universels.
    // Elle NE SAIT PAS si c'est un pressing, une réparation ou des soins.
    // Les notifications sont génériques — basées sur le statut, pas le type.
    //
    // Pour avancer dans les étapes métier (collecte → traitement → livraison),
    // utiliser la méthode avancerEtape() ci-dessous.
    @Transactional
    public CommandeDTO actuliserStatut(Long id, String statut, Long acteurId) {

        Commande c = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        String ancienStatut = c.getStatutCommande().name();
        StatutCommande nouveauStatut = StatutCommande.valueOf(statut);

        validerTransiton(c.getStatutCommande(), nouveauStatut);

        c.setStatutCommande(nouveauStatut);

        // ─── Notifications universelles (Couche 1) ────────────────
        // Aucun if sur typeService — valable pour tout type de service

        if (nouveauStatut == StatutCommande.CONFIRMEE) {
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "✅ Commande confirmée !",
                    "Votre commande #" + id + " a été confirmée par "
                            + c.getPrestataire().getNomCommercial(),
                    "COMMANDE_CONFIRMEE");
        }

        if (nouveauStatut == StatutCommande.EN_COURS) {
            // Notification générique — l'étape précise est dans etapeCourante
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "🔄 Commande en cours !",
                    "Votre commande #" + id + " est en cours de traitement.",
                    "COMMANDE_EN_COURS");
        }

        if (nouveauStatut == StatutCommande.CLOTUREE) {
            fcmService.envoyerNotification(
                    c.getPrestataire().getId(),
                    "✅ Commande clôturée !",
                    "La commande #" + id + " a été clôturée avec succès !",
                    "COMMANDE_CLOTUREE");
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "✅ Merci pour votre confiance !",
                    "Votre commande #" + id + " est clôturée. À bientôt sur ServiProx !",
                    "COMMANDE_CLOTUREE");
            // Libérer l'agent de livraison si existant
            libererAgent(id, "LIVRAISON");
        }

        if (nouveauStatut == StatutCommande.ANNULEE) {
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "❌ Commande annulée !",
                    "Votre commande #" + id + " a été annulée.",
                    "COMMANDE_ANNULEE");
        }

        if (nouveauStatut == StatutCommande.LITIGE) {
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "⚠️ Litige ouvert !",
                    "Un litige a été ouvert sur votre commande #" + id + ". Nous vous contactons.",
                    "LITIGE_OUVERT");
            fcmService.envoyerNotification(
                    c.getPrestataire().getId(),
                    "⚠️ Litige ouvert !",
                    "Un litige a été ouvert sur la commande #" + id + ". Contactez le client.",
                    "LITIGE_OUVERT");
        }

        commandeRepository.save(c);

        evenementSuiviService.enregistrer(
                id, ancienStatut, statut, "CHANGEMENT_STATUT",
                "Statut changé : " + ancienStatut + " → " + statut, acteurId);

        return toDTO(c);
    }

    // ═══════════════════════════════════════════════════════════════
    // AVANCER ÉTAPE — avance l'étape métier DANS EN_COURS
    // ═══════════════════════════════════════════════════════════════
    //
    // ✅ Architecture Couche 2 (mémoire) — MÉTHODE CLÉ :
    // Cette méthode est GÉNÉRIQUE pour TOUS les types de service.
    // Elle ne sait pas si c'est "collecte" (pressing) ou
    // "diagnostic" (réparation) — elle avance juste l'étape.
    //
    // Les étapes valides viennent de ConfigPrestataire.etapesPersonnalisees
    // ou de ConfigurationService.etapes.
    //
    // Exemple d'appel :
    //   pressing    : avancerEtape(id, "traitement", agentId)
    //   reparation  : avancerEtape(id, "reparation", prestataireId)
    //   soins       : avancerEtape(id, "intervention", prestataireId)
    @Transactional
    public CommandeDTO avancerEtape(Long commandeId, String nouvelleEtape, Long acteurId) {

        Commande c = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (c.getStatutCommande() != StatutCommande.EN_COURS) {
            throw new RuntimeException(
                    "Impossible d'avancer l'étape : la commande n'est pas EN_COURS");
        }

        String ancienneEtape = c.getEtapeCourante();
        c.setEtapeCourante(nouvelleEtape);
        commandeRepository.save(c);

        // Notification générique — fonctionne pour TOUT type de service
        fcmService.envoyerNotification(
                c.getClient().getId(),
                "📦 Mise à jour commande #" + commandeId,
                "Étape en cours : " + nouvelleEtape,
                "ETAPE_" + nouvelleEtape.toUpperCase());

        // Libérer l'agent de collecte quand l'étape "collecte" est terminée
        if ("traitement".equals(nouvelleEtape)) {
            libererAgent(commandeId, "COLLECTE");
        }

        evenementSuiviService.enregistrer(
                commandeId,
                ancienneEtape != null ? ancienneEtape : "DEBUT",
                nouvelleEtape,
                "AVANCEMENT_ETAPE",
                "Étape avancée : " + ancienneEtape + " → " + nouvelleEtape,
                acteurId);

        return toDTO(c);
    }

    // ═══════════════════════════════════════════════════════════════
    // ANNULER
    // ═══════════════════════════════════════════════════════════════
    public CommandeDTO annuler(Long id, Long acteurId) {
        Commande c = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        // ✅ Utilise uniquement les statuts universels (Couche 1)
        if (c.getStatutCommande() == StatutCommande.CLOTUREE) {
            throw new RuntimeException("Impossible d'annuler : commande déjà clôturée !");
        }
        if (c.getStatutCommande() == StatutCommande.EN_COURS) {
            throw new RuntimeException("Impossible d'annuler : la prestation est déjà en cours !");
        }

        String ancienStatut = c.getStatutCommande().name();
        c.setStatutCommande(StatutCommande.ANNULEE);
        commandeRepository.save(c);

        evenementSuiviService.enregistrer(
                id, ancienStatut, "ANNULEE",
                "Commande annulée", "COMMANDE_ANNULEE", acteurId);

        return toDTO(c);
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATION TRANSITION STATUT
    // ═══════════════════════════════════════════════════════════════
    //
    // ✅ Simplifié pour les 6 statuts universels (Couche 1)
    // Plus de EN_COLLECTE, EN_TRAITEMENT, TRAITEMENT_TERMINE...
    // Ces étapes sont dans avancerEtape() (Couche 2)
    //
    // Cycle : CREE → CONFIRMEE → EN_COURS → CLOTUREE
    //                   ↓            ↓
    //                ANNULEE       LITIGE → CLOTUREE
    private void validerTransiton(StatutCommande actuel, StatutCommande nouveau) {
        if (actuel == StatutCommande.CLOTUREE || actuel == StatutCommande.ANNULEE) {
            throw new RuntimeException("Commande déjà finalisée !");
        }

        boolean valide = switch (actuel) {
            case CREE ->
                    nouveau == StatutCommande.CONFIRMEE ||
                            nouveau == StatutCommande.ANNULEE;

            case CONFIRMEE ->
                    nouveau == StatutCommande.EN_COURS ||
                            nouveau == StatutCommande.ANNULEE;

            case EN_COURS ->
                    nouveau == StatutCommande.CLOTUREE ||
                            nouveau == StatutCommande.LITIGE ||
                            nouveau == StatutCommande.ANNULEE;

            case LITIGE ->
                    nouveau == StatutCommande.CLOTUREE ||
                            nouveau == StatutCommande.ANNULEE;

            default -> false;
        };

        if (!valide) {
            throw new RuntimeException("Transition invalide : " + actuel + " → " + nouveau);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FIXER PRIX RÉEL — prestataire évalue le prix après vérification
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO fixerPrixReel(Long commandeId, Double prixParTypePrestations,
                                     Double prixCourseCollecte, Double prixCourseLivraison,
                                     String detailPrixReel, Long acteurId) {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        commande.setPrixParTypePrestations(prixParTypePrestations);
        commande.setPrixCourseCollecte(prixCourseCollecte != null ? prixCourseCollecte : 0.0);
        commande.setPrixCourseLivraison(prixCourseLivraison != null ? prixCourseLivraison : 0.0);
        commande.setDetailPrixReel(detailPrixReel);
        commande.setMontantTotal(
                commande.getPrixParTypePrestations()
                        + commande.getPrixCourseCollecte()
                        + commande.getPrixCourseLivraison());

        commandeRepository.save(commande);

        fcmService.envoyerNotification(
                commande.getClient().getId(),
                "💰 Prix réel fixé !",
                "Votre commande #" + commandeId
                        + " a été évaluée. Montant total : "
                        + commande.getMontantTotal() + " FCFA. Acceptez ou refusez !",
                "PRIX_FIXE");

        notificationService.envoyer(
                commande.getClient().getId(),
                "PRIX_CONFIRME",
                "Votre commande #" + commandeId + " a été vérifiée. Montant total : "
                        + commande.getMontantTotal() + " FCFA. Veuillez confirmer !");

        evenementSuiviService.enregistrer(
                commandeId,
                commande.getStatutCommande().name(),
                commande.getStatutCommande().name(),
                "PRIX_FIXE",
                "Prix réel fixé : " + commande.getMontantTotal() + " FCFA",
                acteurId);

        return toDTO(commande);
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCEPTER / REFUSER PRIX — décision du client
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO accepterPrix(Long commandeId, String decision, Long acteurId) {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if ("ACCEPTER".equals(decision)) {
            commande.setPrixAccepteClient(true);
            commandeRepository.save(commande);
            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "✅ Prix accepté !",
                    "Le client a accepté le prix. Commande #" + commandeId + " en traitement.",
                    "PRIX_ACCEPTE");
            // Le prestataire doit ensuite appeler avancerEtape()
            return toDTO(commande);

        } else {
            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "❌ Prix refusé !",
                    "Le client a refusé le prix. Commande #" + commandeId + " annulée.",
                    "PRIX_REFUSE");
            fcmService.envoyerNotification(
                    commande.getClient().getId(),
                    "❌ Commande annulée",
                    "Votre commande #" + commandeId + " a été annulée suite au refus du prix.",
                    "COMMANDE_ANNULEE");
            return actuliserStatut(commandeId, "ANNULEE", acteurId);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ASSIGNER AGENT
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO assignerAgent(Long commandeId, Long agentId, String typeAffectation) {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        if (!agent.getDisponibilite()) {
            throw new RuntimeException("Agent non disponible !");
        }

        affectationRepository.findByCommandeIdAndTypeAffectation(commandeId, typeAffectation)
                .ifPresent(a -> {
                    throw new RuntimeException("Un agent est déjà assigné pour ce type !");
                });

        Affectation affectation = new Affectation();
        affectation.setAgent(agent);
        affectation.setCommande(commande);
        affectation.setStatut("EN_ATTENTE");
        affectation.setTypeAffectation(typeAffectation);
        affectationRepository.save(affectation);

        notificationService.envoyer(
                agent.getId(),
                "NOUVELLE_MISSION",
                "Nouvelle mission " + typeAffectation + " ! Commande #" + commandeId
                        + " — Client : " + commande.getClient().getPrenom()
                        + " " + commande.getClient().getNom() + ". Confirmez votre mission !");

        agent.setDisponibilite(false);
        agentRepository.save(agent);

        evenementSuiviService.enregistrer(
                commandeId,
                commande.getStatutCommande().name(),
                commande.getStatutCommande().name(),
                "ASSIGNATION_AGENT",
                "Agent " + agent.getPrenom() + " " + agent.getNom()
                        + " assigné pour " + typeAffectation,
                agentId);

        return toDTO(commande);
    }

    // ═══════════════════════════════════════════════════════════════
    // LIBÉRER AGENT (méthode privée)
    // ═══════════════════════════════════════════════════════════════
    private void libererAgent(Long commandeId, String typeAffectation) {
        List<Affectation> affectations = affectationRepository.findByCommandeId(commandeId);
        for (Affectation a : affectations) {
            if (typeAffectation.equals(a.getTypeAffectation()) && a.getAgent() != null) {
                a.getAgent().setDisponibilite(true);
                agentRepository.save(a.getAgent());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPER entité → DTO
    // ═══════════════════════════════════════════════════════════════
    public CommandeDTO toDTO(Commande c) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(c.getId());
        dto.setStatut(c.getStatutCommande() != null ? c.getStatutCommande().name() : null);

        // ✅ etapeCourante — étape métier en cours (Couche 2)
        // Ex: "collecte", "traitement", "livraison" pour le pressing
        dto.setEtapeCourante(c.getEtapeCourante());

        dto.setMontantTotal(c.getMontantTotal());
        dto.setDateCommande(c.getDateCommande() != null ? c.getDateCommande().toLocalDate() : null);

        // ✅ modeDepot est un String (plus d'enum) — pas de .name()
        dto.setModeDepot(c.getModeDepot());

        dto.setNomPrestataire(c.getPrestataire().getNomCommercial());
        dto.setDescriptionColis(c.getDescriptionColis());
        dto.setNomClient(c.getClient().getNom() + " " + c.getClient().getPrenom());
        dto.setTelephoneClient(c.getClient().getTelephone());
        dto.setPrixParTypePrestations(c.getPrixParTypePrestations());
        dto.setPrixCourseCollecte(c.getPrixCourseCollecte());
        dto.setPrixCourseLivraison(c.getPrixCourseLivraison());
        dto.setDetailPrixReel(c.getDetailPrixReel());
        dto.setPrixAccepteClient(c.getPrixAccepteClient());

        // Infos agents via affectations
        if (c.getAffectations() != null) {
            for (Affectation a : c.getAffectations()) {
                if (a.getAgent() == null) continue;
                Agent agent = a.getAgent();
                String nomAgent = agent.getPrenom() + " " + agent.getNom();
                String telAgent = agent.getTelephone();
                if ("COLLECTE".equals(a.getTypeAffectation())) {
                    dto.setAgentCollecteId(agent.getId());
                    dto.setNomAgentCollecte(nomAgent);
                    dto.setTelephoneAgentCollecte(telAgent);
                } else if ("LIVRAISON".equals(a.getTypeAffectation())) {
                    dto.setAgentLivraisonId(agent.getId());
                    dto.setNomAgentLivraison(nomAgent);
                    dto.setTelephoneAgentLivraison(telAgent);
                }
            }
        }

        // Lignes de commande
        if (c.getLignesCommande() != null) {
            dto.setLignes(c.getLignesCommande().stream().map(l -> {
                LigneCommandeDTO ld = new LigneCommandeDTO();
                ld.setTypePrestation(l.getTypePrestation());
                ld.setQuantite(l.getQuantite());
                ld.setPrixUnitaire(l.getPrixUnitaire());
                ld.setMontantLigne(l.getQuantite() * l.getPrixUnitaire());
                return ld;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}