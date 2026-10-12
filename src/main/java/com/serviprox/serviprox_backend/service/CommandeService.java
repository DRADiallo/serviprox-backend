package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.CommandeRequest;
import com.serviprox.serviprox_backend.dto.LigneCommandeDTO;
import com.serviprox.serviprox_backend.entity.*;
import com.serviprox.serviprox_backend.enums.ModeDepot;
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
public class CommandeService{
    private  final CommandeRepository commandeRepository;
    private  final ClientRepository clientRepository;
    private  final PrestataireRepository prestataireRepository;
    private  final EvenementSuiviService evenementSuiviService;
    private final AgentRepository agentRepository;
    private final AffectationRepository affectationRepository;
    private final NotificationService notificationService;
    private final FcmService fcmService;


    // ═══ CREATE ═══
    // Client passe une commande
    public CommandeDTO creerCommande(Long clientId, CommandeRequest req){

        Client client= clientRepository
                .findById(clientId)
                .orElseThrow(()-> new RuntimeException("Client Introuvable "));

        Prestataire prestataire= prestataireRepository
                .findById(req.getPrestataireId())
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));

        // Vérifier que le prestataire est actif
        if (prestataire.getStatut()!= StatutActeur.ACTIF){
            throw new RuntimeException("Ce prestataire n est pas disponible ");

        }

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setPrestataire(prestataire);

        // Statut initial toujours CREE
        commande.setStatutCommande(StatutCommande.CREE);

        if (req.getModeDepot()!= null){
            commande.setModeDepot(ModeDepot.valueOf(req.getModeDepot()));
        }

        if (req.getDescriptionColis() != null) {
            commande.setDescriptionColis(
                    req.getDescriptionColis());
        }

        // Lignes de commande
        if (req.getLignes() !=null){
            List<LigneCommande> lignes= req.getLignes()
                    .stream().map(l->{
                        LigneCommande ligneCommande= new LigneCommande();
                        ligneCommande.setTypePrestation(l.getTypePrestation());
                        ligneCommande.setQuantite(l.getQuantite());
                        ligneCommande.setUnite(l.getUnite());
                        ligneCommande.setPrixUnitaire(Double.valueOf(l.getPrixUnitaire()));
                        ligneCommande.setInstructions(l.getInstruction());
                        // Lien vers la commande parente
                        ligneCommande.setCommande(commande);
                        return ligneCommande;
                    }).collect(Collectors.toList());
            commande.setLignesCommande(lignes);

            // Calcul automatique du montant total
            // montant = somme(quantité × prix unitaire)
            double totalprix= lignes.stream()
                    .mapToDouble(l-> l.getQuantite()* l.getPrixUnitaire())
                    .sum();
            commande.setPrixParTypePrestations(totalprix);
            commande.setMontantTotal(totalprix);
        }
        Commande saved = commandeRepository.save(commande);

        fcmService.envoyerNotification(
                saved.getPrestataire().getId(),
                "🔔 Nouvelle commande !",
                "Client " + client.getPrenom()
                        + " " + client.getNom()
                        + " a passé une commande #"
                        + saved.getId(),
                "NOUVELLE_COMMANDE");

        // Enregistrer événement de création
        evenementSuiviService.enregistrer(
                saved.getId(),
                null,
                "CREE",
                "CREATION",
                "Commande creee par le client",
                clientId
        );
        return toDTO(saved);

    }

    // ═══ READ ALL ═══
    // Utilisé par AdminService
    public List<CommandeDTO> getAll(){
        return commandeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY ID ═══
    public CommandeDTO getById(Long id){
        return toDTO(commandeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Commande Introuvable")));

    }

    // ═══ READ BY CLIENT ═══
    // Historique des commandes du client
    // Affiché dans l'onglet "Mes commandes" Flutter
    public List<CommandeDTO> getByClient(Long id){
        return commandeRepository
                .findByClientId(id)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY PRESTATAIRE ═══
    // Tableau de bord du prestataire

    public List<CommandeDTO> getByPrestataire(Long prestatairId){
        return commandeRepository
                .findByPrestataireId(prestatairId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY STATUT ═══
    // Filtrer les commandes par statut
    // Ex: toutes les commandes EN_TRAITEMENT

    public List<CommandeDTO>getByStatut(StatutCommande statutCommande){

        return commandeRepository
                .findByStatutCommande(statutCommande)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }
    // ← Dans CommandeService
    // Pas besoin d'AgentService !
        private void libererAgent(Long commandeId, String typeAffectation) {

        List<Affectation> affectations =
                affectationRepository
                        .findByCommandeId(commandeId);

        for (Affectation a : affectations) {
            if (typeAffectation.equals(
                    a.getTypeAffectation()) &&
                    a.getAgent() != null) {
                // ← Direct repository ✅
                a.getAgent().setDisponibilite(true);
                agentRepository.save(a.getAgent());
            }
        }
    }

    // ═══ READ BY STATUT ═══
    // Filtrer les commandes par statut
    // Ex: toutes les commandes EN_TRAITEMENT
    @Transactional
    public CommandeDTO actuliserStatut(
            Long id, String statut, Long acteurId) {

        Commande c = commandeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande INtrouvable"));

        String ancienStatut =
                c.getStatutCommande().name();
        StatutCommande nouveauStatut =
                StatutCommande.valueOf(statut);

        validerTransiton(
                c.getStatutCommande(), nouveauStatut);

        c.setStatutCommande(nouveauStatut);
        commandeRepository.save(c);

        if (nouveauStatut == StatutCommande.CONFIRMEE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "✅ Commande confirmée !",
                    "Votre commande #" + id
                            + " a été confirmée par "
                            + c.getPrestataire().getNomCommercial(),
                    "COMMANDE_CONFIRMEE");
        }

        if (nouveauStatut == StatutCommande.EN_COLLECTE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    " Agent en route !",
                    "L'agent est en route pour "
                            + "collecter vos articles. "
                            + "Commande #" + id,
                    "EN_COLLECTE");
        }
        if (nouveauStatut ==
                StatutCommande.EN_VERIFICATION) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    " Articles bien reçus !",
                    "Le prestataire a reçu vos articles. "
                            + "Évaluation en cours... "
                            + "Commande #" + id,
                    "ARTICLES_RECUS");
        }

        if (nouveauStatut ==
                StatutCommande.EN_TRAITEMENT) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "✅ Prix accepté !",
                    "Votre commande #" + id
                            + " est en cours de traitement.",
                    "EN_TRAITEMENT");
        }

        if (nouveauStatut ==
                StatutCommande.TRAITEMENT_TERMINE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    " Articles prêts !!!",
                    "Vos articles sont prêts ! "
                            + "Effectuez le paiement "
                            + "pour la commande #" + id,
                    "TRAITEMENT_TERMINE");
        }

        if (nouveauStatut ==
                StatutCommande.EN_LIVRAISON) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "🛵 Livraison en route !",
                    "Vos articles sont en cours "
                            + "de livraison. Commande #" + id,
                    "EN_LIVRAISON");
        }

        if (nouveauStatut ==
                StatutCommande.VERIFICATION_FINALE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "📦 Articles livrés !",
                    "Vos articles ont été livrés ! "
                            + "Vérifiez et confirmez "
                            + "la commande #" + id,
                    "VERIFICATION_FINALE");
            // → PRESTATAIRE notifié
            fcmService.envoyerNotification(
                    c.getPrestataire().getId(),
                    "✅ Livraison effectuée !",
                    "L'agent a livré la commande #"
                            + id + ". En attente client.",
                    "LIVRAISON_EFFECTUEE");
        }

        if (nouveauStatut == StatutCommande.CLOTUREE) {
            // → PRESTATAIRE notifié
            fcmService.envoyerNotification(
                    c.getPrestataire().getId(),
                    " Commande clôturée !",
                    "La commande #" + id
                            + " a été clôturée avec succès !",
                    "COMMANDE_CLOTUREE");
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    " Merci pour votre confiance !",
                    "Votre commande #" + id
                            + " est clôturée. "
                            + "À bientôt sur ServiProx !",
                    "COMMANDE_CLOTUREE");
        }

        if (nouveauStatut == StatutCommande.ANNULEE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "❌ Commande refusée !",
                    "Votre commande #" + id
                            + " a été refusée. "
                            + "Essayez un autre prestataire.",
                    "COMMANDE_ANNULEE");
        }

        if (nouveauStatut == StatutCommande.LITIGE) {
            // → CLIENT notifié
            fcmService.envoyerNotification(
                    c.getClient().getId(),
                    "⚠️ Litige ouvert !",
                    "Un litige a été ouvert "
                            + "sur votre commande #" + id
                            + ". Nous vous contactons.",
                    "LITIGE_OUVERT");
            // → PRESTATAIRE notifié
            fcmService.envoyerNotification(
                    c.getPrestataire().getId(),
                    "⚠️ Litige ouvert !",
                    "Un litige a été ouvert "
                            + "sur la commande #" + id
                            + ". Contactez le client.",
                    "LITIGE_OUVERT");
        }


        // ← REMPLACER les 2 anciens blocs par :
        // Agent collecte libéré après réception
        if (nouveauStatut ==
                StatutCommande.EN_VERIFICATION) {
            libererAgent(id, "COLLECTE");
        }

        // ← Agent livraison libéré à clôture
        if (nouveauStatut ==
                StatutCommande.CLOTUREE) {
            libererAgent(id, "LIVRAISON");
        }

        evenementSuiviService.enregistrer(
                id,
                ancienStatut,
                statut,
                "CHANGEMENT_STATUT",
                "STATUT CHANGE "
                        + ancienStatut
                        + " -> " + statut,
                acteurId
        );

        return toDTO(c);
    }
        // ═══ ANNULER ═══
        // Annulation possible uniquement avant
        // le début de l'exécution (EN_TRAITEMENT)

        public CommandeDTO annuler(Long id, Long acteurId){
            Commande c = commandeRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Commande Introuvable"));

            if (c.getStatutCommande()== StatutCommande.EN_TRAITEMENT ||
                    c.getStatutCommande()== StatutCommande.EN_LIVRAISON ||
                    c.getStatutCommande() == StatutCommande.CLOTUREE) {
                throw new RuntimeException("Impossible d'annuler "+ "la prestation est deja en cours !!");

            }

            String ancienStatut = c.getStatutCommande().name();
            c.setStatutCommande(StatutCommande.ANNULEE);
            commandeRepository.save(c);

            evenementSuiviService.enregistrer(
                    id,
                    ancienStatut,
                    "ANNULEE",
                    "Commande annulee",

                    "COMMANDE_ANNULLEE",
                    acteurId
            );
            return toDTO(c);
        }

    // ═══ VALIDATION TRANSITION STATUT ═══
    // Règles métier du cycle de vie
    // On ne peut pas sauter d'étapes !

    private void validerTransiton(StatutCommande actuel, StatutCommande nouveau) {

        if (actuel == StatutCommande.CLOTUREE || actuel == StatutCommande.ANNULEE) {

            throw new RuntimeException("Commande déjà finalisée !");
        }

        boolean valide = switch (actuel) {
            case CREE ->
                    nouveau == StatutCommande.CONFIRMEE ||
                            nouveau == StatutCommande.ANNULEE;

            case CONFIRMEE ->
                // ← Agent collecte ou dépôt atelier
                    nouveau == StatutCommande.EN_COLLECTE ||
                            nouveau == StatutCommande.EN_VERIFICATION ||
                            nouveau == StatutCommande.ANNULEE;

            // ← NOUVEAU : Agent confirme collecte
            case EN_COLLECTE ->
                    nouveau == StatutCommande.EN_VERIFICATION ||
                            nouveau == StatutCommande.LITIGE;

            case EN_VERIFICATION ->
                // ← Client accepte prix réel
                    nouveau == StatutCommande.EN_TRAITEMENT ||
                            nouveau == StatutCommande.LITIGE;

            case EN_TRAITEMENT ->
                    nouveau == StatutCommande.TRAITEMENT_TERMINE;

            // ← Paiement intégré ici
            // Client paie + Prestataire confirme
            // → EN_LIVRAISON directement !
            case TRAITEMENT_TERMINE ->
                    nouveau == StatutCommande.EN_LIVRAISON ||
                            // ← Si client vient récupérer
                            nouveau == StatutCommande.VERIFICATION_FINALE;

            case EN_LIVRAISON ->
                    nouveau == StatutCommande.VERIFICATION_FINALE;


            // Directement CLOTUREE !
            case VERIFICATION_FINALE ->
                    nouveau == StatutCommande.CLOTUREE ||
                            nouveau == StatutCommande.LITIGE;

            case LITIGE ->
                    nouveau == StatutCommande.CLOTUREE ||
                            nouveau == StatutCommande.ANNULEE;

            default -> false;
        };

        if (!valide) {
            throw new RuntimeException(
                    "Transition invalide : "
                            + actuel + " → " + nouveau);
        }
    }

    //  : assigner agent à une commande
    @Transactional
    public CommandeDTO assignerAgent(Long commandeId, Long agentId, String typeAffectation) {

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        Agent agent = agentRepository
                .findById(agentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Agent introuvable"));

        // Vérifier disponibilité
        if (!agent.getDisponibilite()) {
            throw new RuntimeException(
                    "Agent non disponible !");
        }

        // Vérifier si déjà assigné pour ce type
        affectationRepository
                .findByCommandeIdAndTypeAffectation(
                        commandeId, typeAffectation)
                .ifPresent(a -> {
                    throw new RuntimeException(
                            "Un agent est déjà assigné "
                                    + "pour ce type !");
                });

        // Créer affectation
        Affectation affectation = new Affectation();
        affectation.setAgent(agent);
        affectation.setCommande(commande);
        affectation.setStatut("EN_ATTENTE");
        affectation.setTypeAffectation(
                typeAffectation);
        affectationRepository.save(affectation);
        notificationService.envoyer(
                agent.getId(),
                "NOUVELLE_MISSION",
                " Nouvelle mission " + typeAffectation
                        + " ! Commande #" + commandeId
                        + " — Client : "
                        + commande.getClient().getPrenom()
                        + " " + commande.getClient().getNom()
                        + ". Confirmez votre mission !");

        // Rendre agent indisponible
        agent.setDisponibilite(false);
        agentRepository.save(agent);

        // Enregistrer événement
        evenementSuiviService.enregistrer(
                commandeId,
                commande.getStatutCommande().name(),
                commande.getStatutCommande().name(),
                "ASSIGNATION_AGENT",
                "Agent " + agent.getPrenom()
                        + " " + agent.getNom()
                        + " assigné pour "
                        + typeAffectation,
                agentId
        );

        return toDTO(commande);
    }

    // ← NOUVELLE MÉTHODE dans CommandeService
    @Transactional
    public CommandeDTO fixerPrixReel(Long commandeId, Double prixParTypePrestations, Double prixCourseCollecte,
            Double prixCourseLivraison, String detailPrixReel, Long acteurId) {

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        // ← Fixer les prix
        commande.setPrixParTypePrestations(
                prixParTypePrestations);
        commande.setPrixCourseCollecte(
                prixCourseCollecte != null
                        ? prixCourseCollecte : 0.0);
        commande.setPrixCourseLivraison(
                prixCourseLivraison != null
                        ? prixCourseLivraison : 0.0);

        // ← Détail en JSON
        commande.setDetailPrixReel(detailPrixReel);

        // ← Calcul automatique montantTotal
        commande.setMontantTotal(
                commande.getPrixParTypePrestations()
                        + commande.getPrixCourseCollecte()
                        + commande.getPrixCourseLivraison());

        commandeRepository.save(commande);

        fcmService.envoyerNotification(
                commande.getClient().getId(),
                " Prix réel fixé !",
                "Votre commande #" + commandeId
                        + " a été évaluée selon les vos prestatations commandez. "
                        + "Montant total : "
                        + commande.getMontantTotal()
                        + " FCFA. Acceptez ou refusez !",
                "PRIX_FIXE");

        // ← Notifier le client
        notificationService.envoyer(
                commande.getClient().getId(),
                "PRIX_CONFIRME",
                "Votre commande #" + commandeId
                        + " a été vérifiée. "
                        + "Montant total : "
                        + commande.getMontantTotal()
                        + " FCFA. "
                        + "Veuillez confirmer !");

        // ← Enregistrer événement
        evenementSuiviService.enregistrer(
                commandeId,
                commande.getStatutCommande().name(),
                commande.getStatutCommande().name(),
                "PRIX_FIXE",
                "Prix réel fixé par prestataire : "
                        + commande.getMontantTotal() + " FCFA",
                acteurId);

        return toDTO(commande);
    }

    @Transactional
    public CommandeDTO accepterPrix(Long commandeId, String decision, Long acteurId) {

        Commande commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commande introuvable"));

        if ("ACCEPTER".equals(decision)) {
            // ← Client accepte → EN_TRAITEMENT
            commande.setPrixAccepteClient(true);
            commandeRepository.save(commande);
            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "✅ Prix accepté !",
                    "Le client a accepté le prix. "
                            + "Commande #" + commandeId
                            + " en traitement.",
                    "PRIX_ACCEPTE");

            // ← Passer EN_TRAITEMENT
            return actuliserStatut(
                    commandeId,
                    "EN_TRAITEMENT",
                    acteurId);

        } else {
            // ← Client refuse → ANNULEE
            // ← Notifier prestataire
            fcmService.envoyerNotification(
                    commande.getPrestataire().getId(),
                    "❌ Prix refusé !",
                    "Le client a refusé le prix "
                            + "proposé. Commande #"
                            + commandeId + " annulée.",
                    "PRIX_REFUSE");

            // ← Notifier client
            fcmService.envoyerNotification(
                    commande.getClient().getId(),
                    "❌ Commande annulée",
                    "Votre commande #" + commandeId
                            + " a été annulée suite "
                            + "au refus du prix. "
                            + "Désolé pour la gêne.",
                    "COMMANDE_ANNULEE");

            return actuliserStatut(
                    commandeId,
                    "ANNULEE",
                    acteurId);
        }
    }

    // ═══ MAPPER entité → DTO ═══
    public CommandeDTO toDTO(Commande c) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(c.getId());
        dto.setStatut(c.getStatutCommande() != null
                ? c.getStatutCommande().name() : null);
        dto.setMontantTotal(c.getMontantTotal());
        dto.setDateCommande(c.getDateCommande() != null
                ? c.getDateCommande().toLocalDate() : null);
        dto.setMotDepot(c.getModeDepot() != null
                ? c.getModeDepot().name() : null);
        dto.setNomPrestataire(
                c.getPrestataire().getNomCommercial());

        dto.setDescriptionColis(
                c.getDescriptionColis());

        // Infos client
        dto.setNomClient(c.getClient().getNom()
                + " " + c.getClient().getPrenom());
        dto.setTelephoneClient(
                c.getClient().getTelephone());

        dto.setDescriptionColis(c.getDescriptionColis());
        dto.setPrixParTypePrestations(
                c.getPrixParTypePrestations());
        dto.setPrixCourseCollecte(
                c.getPrixCourseCollecte());
        dto.setPrixCourseLivraison(
                c.getPrixCourseLivraison());
        dto.setDetailPrixReel(c.getDetailPrixReel());
        dto.setPrixAccepteClient(
                c.getPrixAccepteClient());

        // ← AJOUT : infos agents via affectations
        if (c.getAffectations() != null) {
            for (Affectation a : c.getAffectations()) {
                if (a.getAgent() == null) continue;

                Agent agent = a.getAgent();
                String nomAgent = agent.getPrenom()
                        + " " + agent.getNom();
                String telAgent = agent.getTelephone();

                if ("COLLECTE".equals(
                        a.getTypeAffectation())) {
                    dto.setAgentCollecteId(
                            agent.getId());
                    dto.setNomAgentCollecte(nomAgent);
                    dto.setTelephoneAgentCollecte(
                            telAgent);
                } else if ("LIVRAISON".equals(
                        a.getTypeAffectation())) {
                    dto.setAgentLivraisonId(
                            agent.getId());
                    dto.setNomAgentLivraison(nomAgent);
                    dto.setTelephoneAgentLivraison(
                            telAgent);
                }
            }
        }

        // Lignes de commande
        if (c.getLignesCommande() != null) {
            dto.setLignes(c.getLignesCommande()
                    .stream()
                    .map(l -> {
                        LigneCommandeDTO ld =
                                new LigneCommandeDTO();
                        ld.setTypePrestation(
                                l.getTypePrestation());
                        ld.setQuantite(l.getQuantite());
                        ld.setPrixUnitaire(
                                l.getPrixUnitaire());
                        ld.setMontantLigne(
                                l.getQuantite()
                                        * l.getPrixUnitaire());
                        return ld;
                    }).collect(Collectors.toList()));
        }
        return dto;
    }

}

