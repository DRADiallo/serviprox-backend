package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Notification;
import com.serviprox.serviprox_backend.enums.StatutNotification;
import com.serviprox.serviprox_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private  final NotificationRepository notificationRepository;

    // Envoyer une notification
    public Notification envoyer(Long destinataireId, String type, String message ){
        Notification notif= new Notification();
        notif.setDestinataire(destinataireId);
        notif.setType(type);
        notif.setMessage(message);
        return notificationRepository.save(notif);
    }


    // Notification d'un utilisateur
    public List<Notification> getByDestinataire(Long id){
        return  notificationRepository.findByDestinataire(id);
    }


    // Marquer comme lue
    public Notification marquerLue(Long id){
        Notification notif= notificationRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Notification introuvable"));
        notif.setStatut(StatutNotification.LU);
        return notificationRepository.save(notif);
    }
}
