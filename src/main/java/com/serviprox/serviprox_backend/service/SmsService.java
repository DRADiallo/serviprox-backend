package com.serviprox.serviprox_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    // En production → Twilio ou Orange SMS API
    // Pour l'instant → simulation dans les logs
    public void envoyerOtp(
            String telephone, String code) {

        // ← Simulation SMS
        // En production remplacer par vrai SMS
        log.info(
                "╔══════════════════════════════╗");
        log.info(
                "║  SMS ServiProx               ║");
        log.info(
                "║  Destinataire : {}  ║", telephone);
        log.info(
                "║  Votre code   : {}        ║", code);
        log.info(
                "║  Expire dans  : 2 minutes    ║");
        log.info(
                "╚══════════════════════════════╝");
    }
}
