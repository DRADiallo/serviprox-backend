package com.serviprox.serviprox_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serviprox.serviprox_backend.entity.*;
import com.serviprox.serviprox_backend.repository.*;
import com.serviprox.serviprox_backend.dto.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCollecteService {

    private final VerificationCollecteRepository verificationRepository;
    private final CommandeRepository commandeRepository;
    private final CommandeService commandeService;

    // ═══ CRÉER VÉRIFICATION ═══
    // Appelé quand commande passe en
    // EN_VERIFICATION
    @Transactional
    public VerificationCollecteDTO
    creerVerification(Long commandeId) {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() ->
                        new RuntimeException("Commande introuvable"));

        // Vérifier si déjà existante
        if (verificationRepository.findByCommandeId(commandeId)
                .isPresent()) {
            return toDTO(verificationRepository.findByCommandeId(commandeId).get());
        }

        VerificationCollecte verif = new VerificationCollecte();
        verif.setCommande(commande);
        verif.setStatut("EN_ATTENTE");

        return toDTO(verificationRepository.save(verif));
    }

    // ═══ CONFIRMER VÉRIFICATION ═══
    // PRESTATAIRE ou CLIENT confirme
    @Transactional
    public CommandeDTO confirmer(Long commandeId, String acteurType, String statut, String commentaire, Long acteurId) {

        // Créer si pas encore existante
        VerificationCollecte verif = verificationRepository
                        .findByCommandeId(commandeId)
                        .orElseGet(() -> {
                            Commande c = commandeRepository
                                    .findById(commandeId)
                                    .orElseThrow();
                            VerificationCollecte v = new VerificationCollecte();
                            v.setCommande(c);
                            return verificationRepository.save(v);
                        });

        // ← PROBLEME → LITIGE direct !
        if ("PROBLEME".equals(statut)) {
            verif.setStatut("LITIGE");
            verificationRepository.save(verif);

            log.info("⚠️ Problème signalé par {} " + "sur commande {}", acteurType, commandeId);

            // Passer commande en LITIGE
            return commandeService.actuliserStatut(
                            commandeId,
                            "LITIGE",
                            acteurId);
        }

        // ← Enregistrer confirmation OK
        switch (acteurType) {
            case "PRESTATAIRE" -> {
                verif.setConfirmePrestataire(true);
                verif.setCommentairePrestataire(commentaire);
                log.info("✅ Prestataire confirme " + "réception commande {}", commandeId);
            }
            case "CLIENT" -> {
                verif.setConfirmeClient(true);
                verif.setCommentaireClient(
                        commentaire);
                log.info("✅ Client confirme " + "articles commande {}", commandeId);
            }
        }

        verificationRepository.save(verif);

        // ← Les 2 ont confirmé → EN_TRAITEMENT !
        if (Boolean.TRUE.equals(verif.getConfirmePrestataire()) && Boolean.TRUE.equals(
                verif.getConfirmeClient())) {

            verif.setStatut("VALIDE");
            verificationRepository.save(verif);

            log.info(" Vérification VALIDE " + "→ EN_TRAITEMENT commande {}", commandeId);

            return commandeService
                    .actuliserStatut(commandeId, "EN_TRAITEMENT", acteurId);
        }

        return commandeService
                .getById(commandeId);
    }

    // ═══ GET VÉRIFICATION ═══
    public VerificationCollecteDTO getByCommande(Long commandeId) {

        return verificationRepository
                .findByCommandeId(commandeId)
                .map(this::toDTO)
                .orElse(null);
    }

    // ═══ MAPPER ═══
    public VerificationCollecteDTO toDTO(VerificationCollecte v) {

        VerificationCollecteDTO dto = new VerificationCollecteDTO();
        dto.setId(v.getId());
        dto.setCommandeId(v.getCommande().getId());
        dto.setConfirmePrestataire(v.getConfirmePrestataire());
        dto.setCommentairePrestataire(v.getCommentairePrestataire());
        dto.setConfirmeClient(v.getConfirmeClient());
        dto.setCommentaireClient(v.getCommentaireClient());
        dto.setStatut(v.getStatut());
        dto.setDateCreation(v.getDateCreation());
        return dto;
    }
}
