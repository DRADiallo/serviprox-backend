package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.NotificationDTO;
import com.serviprox.serviprox_backend.entity.Notification;
import com.serviprox.serviprox_backend.enums.StatutNotification;
import com.serviprox.serviprox_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private  final NotificationRepository notificationRepository;


    // ═══ CREATE — Envoyer une notification ═══
    // Appelé automatiquement par tous les services
    // à chaque événement clé
    //
    // Exemples au Sénégal :
    // → Client    : "Votre commande est confirmée"
    // → Agent     : "Nouvelle mission assignée"
    // → Presta    : "Paiement libéré"
    // → Client    : "Litige ouvert"
    public NotificationDTO envoyer(Long destinataireId, String type, String message ){
        Notification notif= new Notification();
        notif.setDestinataire(destinataireId);
        notif.setType(type);
        notif.setMessage(message);
        notif.setDateEnvoi(LocalDateTime.now());
        // NON_LU par défaut
        notif.setStatut(StatutNotification.NON_LU);
        return toDTO( notificationRepository.save(notif));
    }

    // ═══ READ BY ID ═══
    public NotificationDTO getById(Long id) {
        return toDTO(notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification introuvable")));
    }

    // Toutes les notifications d'un utilisateur
    // Flutter affiche la liste dans
    // l'onglet "Notifications"
    public List<NotificationDTO> getByDestinataire(Long destinataireId){

        return  notificationRepository.findByDestinataire(destinataireId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ NON LUES ═══
    // Badge rouge sur l'icône notification
    // Flutter affiche le nombre de non lues

    public List<NotificationDTO> getNonLues(Long destinataireId){

        return  notificationRepository.findByDestinataireAndStatut(destinataireId,
                        StatutNotification.NON_LU)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ ALL ═══
    // Admin React — supervision

    public List<NotificationDTO> getAll(){

        return  notificationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ UPDATE — MARQUER LUE ═══
    // Client ouvre la notification
    // → statut NON_LU → LU
    // → badge rouge disparaît dans Flutter

    public NotificationDTO marquerLue(Long id){

        Notification notif= notificationRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Notification introuvable"));
        notif.setStatut(StatutNotification.LU);
        return  toDTO(notificationRepository.save(notif));
    }

    // ═══ UPDATE — MARQUER TOUTES LUES ═══
    // Client clique "Tout marquer comme lu"
    // dans Flutter
    public void marquerToutesLues(
            Long destinataireId) {
        List<Notification> nonLues = notificationRepository.findByDestinataireAndStatut(
                destinataireId, StatutNotification.NON_LU);
        nonLues.forEach( n-> n.setStatut(StatutNotification.LU));
        notificationRepository.saveAll(nonLues);

    }

    // ═══ DELETE ═══
    // Supprimer une notification
    // Uniquement par l'utilisateur concerné
    public void supprimer(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Notification introuvable");
        }
        notificationRepository.deleteById(id);
    }

    // ═══ DELETE TOUTES ═══
    // Vider toutes les notifications
    // d'un utilisateur
    public void supprimerToutesLesNotifsUsers(Long destinataireId) {
        List<Notification>notifs = notificationRepository
                .findByDestinataire(destinataireId);
        notificationRepository.deleteAll(notifs);
    }

    // ═══ MAPPER entité → DTO ═══
    public NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setDestinataire(n.getDestinataire());
        dto.setType(n.getType());
        dto.setMessage(n.getMessage());
        dto.setDateEnvoi(n.getDateEnvoi());
        dto.setStatut(n.getStatut() != null
                ? n.getStatut().name() : null);
        return dto;
    }



}
