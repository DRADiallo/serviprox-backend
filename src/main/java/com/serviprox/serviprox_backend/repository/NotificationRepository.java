package com.serviprox.serviprox_backend.repository;

import com.serviprox.serviprox_backend.entity.Notification;
import com.serviprox.serviprox_backend.enums.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByDestinataire(Long destinataire);
    List<Notification> findByDestinataireAndStatut(
            Long destinataire, StatutNotification statut);
}
