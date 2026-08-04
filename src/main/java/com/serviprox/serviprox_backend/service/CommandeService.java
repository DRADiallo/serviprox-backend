package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.CommandeRequest;
import com.serviprox.serviprox_backend.dto.LigneCommandeDTO;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.entity.LigneCommande;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.ModeDepot;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeService{
    private  final CommandeRepository commandeRepository;
    private  final ClientRepository clientRepository;
    private  final PrestataireRepository prestataireRepository;
    private  final EvenementSuiviService evenementSuiviService;

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
            commande.setMontantTotal(totalprix);
        }
        Commande saved = commandeRepository.save(commande);

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
                .findById(id)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY PRESTATAIRE ═══
    // Tableau de bord du prestataire

    public List<CommandeDTO> getByPrestataire(Long prestatairId){
        return commandeRepository
                .findById(prestatairId)
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

    // ═══ READ BY STATUT ═══
    // Filtrer les commandes par statut
    // Ex: toutes les commandes EN_TRAITEMENT

    public CommandeDTO actuliserStatut(Long id, String statut, Long acteurId) {

        Commande c = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande INtrouvable"));

        String ancienStatut = c.getStatutCommande().name();
        StatutCommande nouveauStatut = StatutCommande.valueOf(statut);

        // Validation du changement de statut
        // On ne peut pas revenir en arrière !
        validerTransiton(c.getStatutCommande(), nouveauStatut);

        c.setStatutCommande(nouveauStatut);
        commandeRepository.save(c);

        // Enregistrer dans l'historique
        evenementSuiviService.enregistrer(
                id,
                ancienStatut,
                statut,
                "CHANGEMENT_STATUT",
                "STATUT CHANGE"
                        + ancienStatut + "-> " + statut, acteurId

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
                    c.getStatutCommande()== StatutCommande.TERMINEE ||
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

    private void validerTransiton(StatutCommande actuel, StatutCommande nouveau){

        // commande finale aucune transition possible

        if (actuel== StatutCommande.CLOTUREE ||
            actuel== StatutCommande.ANNULEE){
            throw new RuntimeException("Commande  déjà finalisée" + "aucune modification possible");

        }

        // TRansition autorisees

        boolean valide = switch (actuel){
            case CREE -> nouveau == StatutCommande.CONFIRMEE
                    || nouveau == StatutCommande.ANNULEE;
            case CONFIRMEE -> nouveau == StatutCommande.EN_TRAITEMENT
                    || nouveau == StatutCommande.ANNULEE;
            case EN_TRAITEMENT ->  nouveau == StatutCommande.EN_LIVRAISON
                    || nouveau == StatutCommande.TERMINEE;
            case EN_LIVRAISON -> nouveau== StatutCommande.TERMINEE;
            case TERMINEE -> nouveau == StatutCommande.CLOTUREE
                    || nouveau == StatutCommande.LITIGE;
            case LITIGE -> nouveau == StatutCommande.CLOTUREE
                    || nouveau == StatutCommande.ANNULEE;
            default -> false;
        };
        if (!valide){
            throw new RuntimeException("Transition invalide :"+ actuel+ "->" + nouveau);

        }
    }

    // ═══ MAPPER entité → DTO ═══
    public   CommandeDTO toDTO(Commande c){
        CommandeDTO dto= new CommandeDTO();
        dto.setId(c.getId());
        dto.setStatut(c.getStatutCommande() !=null? c.getStatutCommande().name():null);
        dto.setMontantTotal(c.getMontantTotal());
        dto.setDateCommande(c.getDateCommande() !=null? c.getDateCommande().toLocalDate(): null);
        dto.setMotDepot(c.getModeDepot() !=null?c.getModeDepot().name():null);
        dto.setNomPrestataire(c.getPrestataire().getNomCommercial());
        dto.setNomClient(c.getClient().getNom() + " " +c.getClient().getPrenom());

        // Lignes de Commandes
        if (c.getLignesCommande() !=null){
            dto.setLignes(c.getLignesCommande()
                    .stream()
                    .map(l->{
                        LigneCommandeDTO ld= new LigneCommandeDTO();
                        ld.setTypePrestation(l.getTypePrestation());
                        ld.setQuantite(l.getQuantite());
                        ld.setPrixUnitaire(l.getPrixUnitaire());
                        ld.setMontantLigne(l.getQuantite()*l.getPrixUnitaire());
                        return ld;


                    }).collect(Collectors.toList()));

        }
        return dto;
    }

}

