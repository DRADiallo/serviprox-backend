package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.NotificationDTO;
import com.serviprox.serviprox_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // GET /api/notification/destinataire/{id}
    // Toutes les notifications d'un utilisateur
    @GetMapping("/destinataire/{id}")
    public ResponseEntity<List<NotificationDTO>> getByDestinataire(@PathVariable Long id){

        return ResponseEntity.ok(notificationService.getByDestinataire(id));
    }

    // PUT /api/notification/{id}/lire
    // Marquer une notification comme lue
    @PutMapping("/{id}/lire")
    public ResponseEntity<NotificationDTO>marqueLue(@PathVariable Long id){

        return ResponseEntity.ok(notificationService.marquerLue(id));
    }


    // GET /api/notification/non-lues/{id}
    // Badge rouge Flutter
    @PutMapping("/non-lues/{id}")
    public ResponseEntity<List<NotificationDTO>>getNonLues(@PathVariable Long id){

        return ResponseEntity.ok(notificationService.getNonLues(id));
    }

    // PUT /api/notification/toutes-lues/{id}
    // Marquer toutes les notifications lues
    @PutMapping("/toutes-lues/{id}")
    public ResponseEntity<Void> marquerToutesLues(@PathVariable Long id){

        notificationService.marquerToutesLues(id);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/notification/{id}
    // Supprimer une notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>supprimer(@PathVariable Long id ){

       notificationService.supprimer(id);
        return ResponseEntity.ok().build();
    }


}
