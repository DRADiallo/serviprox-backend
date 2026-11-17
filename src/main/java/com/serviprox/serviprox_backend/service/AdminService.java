package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.EvaluationDTO;
import com.serviprox.serviprox_backend.dto.PrestataireDTO;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.EvaluationRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import com.serviprox.serviprox_backend.service.PrestataireService;
import com.serviprox.serviprox_backend.service.CommandeService;
import com.serviprox.serviprox_backend.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final PrestataireRepository prestataireRepository;

    private final ClientRepository clientRepository;

    private final CommandeRepository commandeRepository;

    private final EvaluationRepository evaluationRepository;

    private final NotificationService notifService;

    private final PrestataireService prestataireService;

    private final CommandeService commandeService;

    private final EvaluationService evaluationService;

    // ═══════════════════════════════════════
    // DASHBOARD REACT
    // GET /api/admin/dashboard/stats
    // ═══════════════════════════════════════

    /**
     * Statistiques tableau de bord React
     * <p>
     * L'admin voit en un coup d'œil :
     * - Clients inscrits
     * - Prestataires par statut
     * - Commandes par statut
     * - Litiges en cours
     * - Taux de complétion
     */

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Compteurs Globlaux

        stats.put("totalClients", clientRepository.count());
        stats.put("totalPrstataires", prestataireRepository.count());
        stats.put("totalCommandes", commandeRepository.count());

        // Prestataire Par Statut
        stats.put("prestatairesActifs", prestataireRepository
                .findByStatut(StatutActeur.ACTIF)
                .size());

        stats.put("prestatairesEnAttente", prestataireRepository
                .findByStatut(StatutActeur.EN_ATTENTE_VALIDATION)
                .size());

        stats.put("prestatairesSuspendu", prestataireRepository
                .findByStatut(StatutActeur.INACTIF)
                .size());

        // Commandes Par Statut

        //stats.put("commandesEnCours", commandeRepository.findByStatutCommande(StatutCommande.EN_TRAITEMENT).size());

        stats.put("commandesEnCloturees", commandeRepository
                .findByStatutCommande(StatutCommande.CLOTUREE)
                .size());

        stats.put("commandesLitiges", commandeRepository
                .findByStatutCommande(StatutCommande.LITIGE)
                .size());

        stats.put("commandesAnnulees", commandeRepository
                .findByStatutCommande(StatutCommande.ANNULEE)
                .size());

        // Taux de complétion
        // Idealement > 90%

        long total = commandeRepository.count();
        long cloturees = commandeRepository
                .findByStatutCommande(StatutCommande.CLOTUREE)
                .size();
        stats.put("tauxCompletion", total > 0 ? Math.round((double) cloturees / total * 100 * 100) / 100.0 : 0);

        // Taux de Litiges
        // Idealement < 5%
        long litiges = commandeRepository
                .findByStatutCommande(StatutCommande.LITIGE)
                .size();
        stats.put("tauxLItiges", total > 0 ? Math.round((double) litiges / total * 100 * 100) / 100.0 : 0);

        return stats;

    }

    // ═══════════════════════════════════════
    // GESTION PRESTATAIRES
    // ═══════════════════════════════════════

    /**
     * Prestataires en attente de validation
     * GET /api/admin/prestataires/en-attente
     * <p>
     * Process au Sénégal :
     * 1. Prestataire s'inscrit
     * 2. Admin vérifie par appel ou visite
     * 3. Admin valide → visible aux clients
     */

    public List<PrestataireDTO> getPrestatairesEnAttente() {

        return prestataireRepository
                .findByStatut(StatutActeur.EN_ATTENTE_VALIDATION)
                .stream()
                .map(prestataireService::toDTO)
                .collect(Collectors.toList());
    }

    // GET /api/admin/prestataires
    public List<PrestataireDTO> getAllPrestataires() {

        return prestataireRepository
                .findAll()
                .stream()
                .map(prestataireService::toDTO)
                .collect(Collectors.toList());
    }

    // GET /api/admin/prestataires/actifs
    public List<PrestataireDTO> getPrestatairesActifs() {

        return prestataireRepository
                .findByStatut(StatutActeur.ACTIF)
                .stream()
                .map(prestataireService::toDTO)
                .collect(Collectors.toList());
    }

    // GET /api/admin/prestataires/actifs
    public List<PrestataireDTO> getPrestatairesSuspendu() {

        return prestataireRepository
                .findByStatut(StatutActeur.SUSPENDU)
                .stream()
                .map(prestataireService::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Valider un prestataire
     * PUT /api/admin/prestataires/{id}/valider
     * <p>
     * → Statut EN_ATTENTE_VALIDATION → ACTIF
     * → Visible aux clients Flutter
     * → Notification push au prestataire
     */
    public PrestataireDTO validerPrestataire(Long prestataireId) {

        Prestataire p = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire Introuvable ! "));

        p.setStatut(StatutActeur.ACTIF);
        prestataireRepository.save(p);

        notifService.envoyer(prestataireId,
                "COMPTE_VALIDE",
                "Félicitations ! Votre compte"
                        + p.getNomCommercial()
                        + "a été validé"
                        + "Configurez votre service"
                        + "Configurez votre service des commandes");

        return prestataireService.toDTO(p);
    }

    /**
     * Rejeter un prestataire
     * PUT /api/admin/prestataires/{id}/rejeter
     * <p>
     * Cas de rejet au Sénégal :
     * - Informations incomplètes ou fausses
     * - Zone déjà saturée de prestataires
     * - Activité non conforme
     */
    public PrestataireDTO rejeterPrestataire(Long prestataireId, String motif) {

        Prestataire p = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire Introuvable ! "));

        p.setStatut(StatutActeur.INACTIF);
        prestataireRepository.save(p);

        notifService.envoyer(prestataireId,
                "COMPTE_REJETE",
                " Votre demande d'inscription"
                        + p.getNomCommercial()
                        + "a été rejetée"
                        + "\\nMotif : " + motif
                        + "\\nVous pouvez soumettre"
                        + "\nVous pouvez soumettre");

        return prestataireService.toDTO(p);
    }

    /**
     * Suspendre un prestataire
     * PUT /api/admin/prestataires/{id}/suspendre
     * <p>
     * Cas de suspension au Sénégal :
     * - Articles perdus ou endommagés répétés
     * - Plaintes clients multiples
     * - Comportement frauduleux signalé
     */

    public PrestataireDTO suspendrePrestataire(Long prestataireId, String motif) {

        Prestataire p = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire Introuvable ! "));

        p.setStatut(StatutActeur.SUSPENDU);
        prestataireRepository.save(p);

        notifService.envoyer(prestataireId,
                "COMPTE_SUSPENDU",
                " ⛔ Votre compte a été suspendu"
                        + p.getNomCommercial()
                        + "\\nMotif : " + motif
                        + "\\\nContactez le support :"
        );

        return prestataireService.toDTO(p);
    }

    /**
     * Réactiver un prestataire suspendu
     * PUT /api/admin/prestataires/{id}/reactiver
     * <p>
     * Après investigation et résolution
     * du problème ayant causé la suspension
     */

    public PrestataireDTO ractiverPrestataire(Long prestataireId) {

        Prestataire p = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire Introuvable ! "));

        p.setStatut(StatutActeur.ACTIF);
        prestataireRepository.save(p);

        notifService.envoyer(prestataireId,
                "COMPTE_REACTIVE",
                "Votre compte a été réactivé !"
                        + "Vous pouvez à nouveau"
                        + "recevoir des commandes");

        return prestataireService.toDTO(p);
    }

    // ═══════════════════════════════════════
    // GESTION COMMANDES
    // GET /api/admin/commandes
    // ═══════════════════════════════════════

    /**
     * Toutes les commandes
     * Vue globale pour l'admin React
     */
    public List<CommandeDTO>getAllCommandes(){

        return commandeRepository
                .findAll()
                .stream()
                .map(commandeService::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════
    // GESTION LITIGES
    // GET /api/admin/litiges
    // ═══════════════════════════════════════

    /**
     * Tous les litiges en cours
     * L'admin peut intervenir si nécessaire
     * comme médiateur entre client et prestataire
     */
    public List<CommandeDTO>getAllCommandesLitiges(){

        return commandeRepository
                .findByStatutCommande(StatutCommande.LITIGE)
                .stream()
                .map(commandeService::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════
    // STATISTIQUES AVANCÉES
    // GET /api/admin/statistiques
    // ═══════════════════════════════════════

    /**
     * Statistiques avancées pour graphiques React
     * Chart.js affiche :
     * - Camembert : commandes par statut
     * - Barres : prestataires par statut
     * - KPI : taux litige, taux complétion
     */
    public Map<String, Object>getStatistiquesAvancees(){
        Map<String,Object> stats = new HashMap<>();

        long total = commandeRepository.count();

        // ── Commandes par statut ──
        // Pour graphique camembert React
        Map<String, Long> commmandesParStatut = new HashMap<>();

        for (StatutCommande s : StatutCommande.values()){
            commmandesParStatut.put(s.name(),
                    (long) commandeRepository
                            .findByStatutCommande(s)
                            .size());
        }
        stats.put("commandesParStatu",commmandesParStatut);

        // Prestataires par statut
        // Pour graphique barres React
        Map<String, Long> prestataireParStatut = new HashMap<>();
        prestataireParStatut.put("actifs", (long) prestataireRepository
                .findByStatut(StatutActeur.ACTIF)
                .size());
        prestataireParStatut.put("enAttente",(long) prestataireRepository
                .findByStatut(StatutActeur.EN_ATTENTE_VALIDATION)
                .size());

        prestataireParStatut.put("suspendus",(long) prestataireRepository
                .findByStatut(StatutActeur.SUSPENDU)
                .size());

        stats.put("prestataireParStaut", prestataireParStatut);

        // KPIs
        long litiges = commandeRepository
                .findByStatutCommande(StatutCommande.LITIGE).size();

        long cloturees = commandeRepository
                .findByStatutCommande(StatutCommande.CLOTUREE).size();

        stats.put("tauxLitige", total > 0 ? Math.round((double) litiges / total * 100 *100.0):0);

        stats.put("tauxCompletion", total > 0 ? Math.round((double) cloturees / total * 100 * 100.0):0);

        stats.put("totalClients", clientRepository.count());

        stats.put("totalPrestataires", prestataireRepository.count());

        stats.put("totalCommandes", total);

        return  stats;
    }
    // ═══════════════════════════════════════
    // MODÉRATION ÉVALUATIONS
    // GET /api/admin/evaluations
    // ═══════════════════════════════════════

    /**
     * Toutes les évaluations
     * Admin peut modérer les avis abusifs
     */
    public List<EvaluationDTO> getAllEvalutions(){

        return evaluationRepository.findAll()
                .stream()
                .map(evaluationService::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer une évaluation abusive
     * DELETE /api/admin/evaluations/{id}
     *
     * Ex: évaluation injurieuse ou fausse
     * → Admin la supprime après vérification
     */

    public void supprimerEvalution(Long evalutionId){

        if (!evaluationRepository.existsById(evalutionId)){
            throw new RuntimeException("Evaluation Introuvable");
        }
    }

}











