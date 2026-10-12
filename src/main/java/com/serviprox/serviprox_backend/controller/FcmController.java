package com.serviprox.serviprox_backend.controller;
import com.serviprox.serviprox_backend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    // ← Sauvegarder token FCM
    @PostMapping("/fcm-token")
    public ResponseEntity<String> saveToken(
            @RequestParam Long userId,
            @RequestParam String role,
            @RequestParam String fcmToken) {

        fcmService.sauvegarderToken(
                userId, role, fcmToken);

        return ResponseEntity.ok(
                "Token FCM sauvegardé !");
    }
}
