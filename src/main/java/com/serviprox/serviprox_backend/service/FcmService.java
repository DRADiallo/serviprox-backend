package com.serviprox.serviprox_backend.service;

import com.google.firebase.messaging.*;
import com.serviprox.serviprox_backend.entity.FcmToken;
import com.serviprox.serviprox_backend.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository
            fcmTokenRepository;

    // ← Sauvegarder token FCM
    @Transactional
    public void sauvegarderToken(
            Long userId,
            String role,
            String fcmToken) {

        FcmToken token = fcmTokenRepository
                .findByUserIdAndRole(userId, role)
                .orElse(new FcmToken());

        token.setUserId(userId);
        token.setRole(role);
        token.setToken(fcmToken);
        token.setUpdatedAt(LocalDateTime.now());

        fcmTokenRepository.save(token);
        log.info("✅ FCM Token sauvegardé "
                + "pour userId: " + userId);
    }

    // ← Envoyer notification à un utilisateur
    public void envoyerNotification(
            Long userId,
            String titre,
            String corps,
            String type) {

        List<FcmToken> tokens =
                fcmTokenRepository
                        .findByUserId(userId);

        if (tokens.isEmpty()) {
            log.warn("⚠️ Pas de FCM token "
                    + "pour userId: " + userId);
            return;
        }

        for (FcmToken fcmToken : tokens) {
            try {
                Message message = Message.builder()
                        .setNotification(
                                Notification.builder()
                                        .setTitle(titre)
                                        .setBody(corps)
                                        .build())
                        .putData("type", type)
                        .putData("userId",
                                userId.toString())
                        .setToken(fcmToken.getToken())
                        .build();

                String response =
                        FirebaseMessaging.getInstance()
                                .send(message);

                log.info("✅ Notification envoyée: "
                        + response);

            } catch (FirebaseMessagingException e) {
                log.error("❌ Erreur FCM: "
                        + e.getMessage());
                // ← Token invalide → supprimer
                if (e.getMessagingErrorCode() ==
                        MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.delete(
                            fcmToken);
                }
            }
        }
    }
}