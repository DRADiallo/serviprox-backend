package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.config.JwtUtils;
import com.serviprox.serviprox_backend.dto.AuthRequest;
import com.serviprox.serviprox_backend.dto.AuthResponse;
import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
import com.serviprox.serviprox_backend.dto.RegisterPrestataireRequest;
import com.serviprox.serviprox_backend.entity.Agent;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.entity.OtpCode;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ClientRepository clientRepository;
    private final PrestataireRepository prestataireRepository;
    private final ActeurRepository acteurRepository;
    private final AgentRepository agentRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final OtpRepository otpRepository;
    private final SmsService smsService;
    //private final PasswordEncoder passwordEncoder;


    public AuthResponse registerClient(RegisterClientRequest req){
        if (acteurRepository.existsByEmail(req.getEmail())){
            throw new RuntimeException("Email déjà utilisé");
        }
        Client client= new Client();
        client.setNom(req.getNom());
        client.setPrenom(req.getPrenom());
        client.setEmail(req.getEmail());
        client.setTelephone(req.getTelephone());
        client.setAdressePrincipale(req.getAdressePrincipale());
        client.setMotDePasse(
                passwordEncoder.encode(req.getMotDePasse()));
        client.setStatut(StatutActeur.ACTIF);
        clientRepository.save(client);

        return new AuthResponse(
                jwtUtils.genererToken(client.getId(), "CLIENT"),
                "CLIENT",
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getAdressePrincipale() != null
                        ? client.getAdressePrincipale() : ""
        );
    }

    public AuthResponse registerPrestataire(RegisterPrestataireRequest req){
        if (acteurRepository.existsByEmail(req.getEmail())){
            throw new RuntimeException("Email déjà utilisé ");
        }
        Prestataire p = new Prestataire();
        p.setNom(req.getNom());
        p.setPrenom(req.getPrenom());
        p.setNomCommercial(req.getNomCommercial());
        p.setEmail(req.getEmail());
        p.setTelephone(req.getTelephone());
        p.setZoneCouverture(req.getZoneCouverture());
        p.setMotDePasse(
                passwordEncoder.encode(req.getMotDePasse()));
        p.setTypeService(req.getTypeService());

        p.setStatut(StatutActeur.EN_ATTENTE_VALIDATION);
        prestataireRepository.save(p);

        return new AuthResponse(
                jwtUtils.genererToken(p.getId(), "PRESTATAIRE"),
                "PRESTATAIRE",
                p.getId(),
                p.getNom(),
                p.getPrenom(),
                p.getEmail(),
                p.getZoneCouverture() !=null ? p.getZoneCouverture():""


        );


    }

    // ← MODIFICATION login()
// Retourne un message au lieu du token
// Le token sera retourné après vérification OTP
    @Transactional
    public Map<String, String> login(AuthRequest req) {

        // ← AJOUT : normaliser le téléphone
        // Enlever l'indicatif +221 si présent
//        String telephoneNormalise = req.getTelephone()
//                .replaceAll("\\+221", "")  // enlever +221
//                .replaceAll("\\s", "")     // enlever espaces
//                .trim();

        // Comparer avec le téléphone en base

        // 1. Vérifier email
        var acteur = acteurRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Email introuvable"));

        // 2. Vérifier mot de passe
        if (!passwordEncoder.matches(
                req.getMotDePasse(),
                acteur.getMotDePasse())) {
            throw new RuntimeException(
                    "Mot de passe incorrect");
        }

        // 3. Vérifier téléphone

        // 3. Vérifier téléphone — cast selon le sous-type
        String telephoneActeur = "";
        if (acteur instanceof Client c) {
            telephoneActeur = c.getTelephone() != null ? c.getTelephone() : "";
        } else if (acteur instanceof Prestataire p) {
            telephoneActeur = p.getTelephone() != null ? p.getTelephone() : "";
        }

// Normaliser les deux : enlever +221 et espaces
        String telNorm = req.getTelephone()
                .replaceAll("\\+221", "").replaceAll("\\s", "").trim();
        String telBase = telephoneActeur
                .replaceAll("\\+221", "").replaceAll("\\s", "").trim();

        if (!telBase.isEmpty() && !telBase.equals(telNorm)) {
            throw new RuntimeException("Numéro de téléphone incorrect");
        }
//        if (!acteur.getTelephone().equals(
//                req.getTelephone())) {
//            throw new RuntimeException(
//                    "Numéro de téléphone incorrect");
//        }
//        if (!acteur.getTelephone().equals(
//                telephoneNormalise)) {
//            throw new RuntimeException(
//                    "Numéro de téléphone incorrect");
//        }

        // 4. Supprimer anciens OTPs
        otpRepository.deleteByTelephone(req.getTelephone());

        // 5. Générer OTP à 6 chiffres
        String otpClair = String.valueOf(
                100000 + new Random().nextInt(900000));

        // 6. Sauvegarder OTP hashé
        OtpCode otp = new OtpCode();
        otp.setTelephone(req.getTelephone());
        otp.setCode(
                passwordEncoder.encode(otpClair));
        otp.setActeurId(acteur.getId());
        // ← Expire dans 2 minutes
        otp.setExpireAt(
                LocalDateTime.now().plusMinutes(2));
        otpRepository.save(otp);

        // 7. Envoyer SMS
        smsService.envoyerOtp(req.getTelephone(), otpClair);

        // 8. Retourner message
        return Map.of(
                "message",
                "Code OTP envoyé au "
                        + req.getTelephone(),
                "telephone",
                req.getTelephone()
        );
    }

    // ← AJOUT : verifyOtp()
// Étape 2 du login
    public AuthResponse verifyOtp(
            String telephone, String code) {

        // 1. Chercher OTP non utilisé
        OtpCode otp = otpRepository
                .findByTelephoneAndUtiliseFalse(telephone)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Code OTP introuvable"));

        // 2. Vérifier expiration
        if (LocalDateTime.now().isAfter(
                otp.getExpireAt())) {
            throw new RuntimeException(
                    "Code OTP expiré");
        }

        // 3. Vérifier code
        if (!passwordEncoder.matches(code, otp.getCode())) {
            throw new RuntimeException("Code OTP incorrect");
        }

        // 4. Marquer OTP comme utilisé
        otp.setUtilise(true);
        otpRepository.save(otp);

        // 5. Charger l'acteur
        var acteur = acteurRepository
                .findById(otp.getActeurId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Acteur introuvable"));

        // ✅ 6. Vérifier statut du prestataire
        if (acteur instanceof Prestataire p) {
            if (p.getStatut() == StatutActeur.EN_ATTENTE_VALIDATION) {
                throw new RuntimeException(
                        "Compte en attente de validation par l'administrateur");
            }
            if (p.getStatut() == StatutActeur.SUSPENDU
                    || p.getStatut() == StatutActeur.INACTIF) {
                throw new RuntimeException(
                        "Compte suspendu/inactif. Contactez l'administrateur.");
            }
        }

        // 7. Retourner token JWT
        String adresse = "";
        if (acteur instanceof Client client) {
            adresse = client
                    .getAdressePrincipale() != null
                    ? client.getAdressePrincipale()
                    : "";
        } else if (acteur instanceof Prestataire p) {
            adresse = p.getZoneCouverture() != null
                    ? p.getZoneCouverture() : "";
        }

//        // ✅ FIX : normaliser le rôle — retirer le préfixe ROLE_ si présent
//        // → registerPrestataire stocke "PRESTATAIRE", verifyOtp doit faire pareil
//        // → Évite "ROLE_ROLE_PRESTATAIRE" dans JwtAuthFilter → 403
//        String roleStr = acteur.getRole().name();
//        if (roleStr.startsWith("ROLE_")) {
//            roleStr = roleStr.substring(5); // "ROLE_PRESTATAIRE" → "PRESTATAIRE"
//        }

        return new AuthResponse(
                jwtUtils.genererToken(
                        acteur.getId(),
                        acteur.getRole().name()),
                acteur.getRole().name(),
                acteur.getId(),
                acteur.getNom(),
                acteur.getPrenom(),
                acteur.getEmail(),
                adresse
        );
    }

    @Transactional
    public Map<String, String> resendOtp (String telephone) {

        // Supprimer ancien OTP
        otpRepository.deleteByTelephone(telephone);

        // Chercher l'acteur par téléphone
        var acteur = acteurRepository
                .findByTelephone(telephone)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Téléphone introuvable"));

        // Générer nouveau OTP
        String otpClair = String.valueOf(
                100000 + new Random().nextInt(900000));

        OtpCode otp = new OtpCode();
        otp.setTelephone(telephone);
        otp.setCode(
                passwordEncoder.encode(otpClair));
        otp.setActeurId(acteur.getId());
        otp.setExpireAt(
                LocalDateTime.now().plusMinutes(2));
        otpRepository.save(otp);

        smsService.envoyerOtp(telephone, otpClair);

        return Map.of(
                "message",
                "Nouveau code envoyé au " + telephone);
    }

        // ═══════════════════════════════════════
        // LOGIN AGENT
        // ───────────────────────────────────────
        // Login par téléphone + code PIN
        // L'agent ne connaît pas son email
        // → Prestataire lui communique son PIN
        // ═══════════════════════════════════════
        // ← MODIFICATION : loginAgent()
        // Étape 1 : téléphone seulement → envoie OTP

    @Transactional
    public Map<String, String> loginAgent(String telephone) {

            // 1. Chercher agent par téléphone
            Agent agent = agentRepository
                    .findByTelephone(telephone)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Téléphone introuvable"));

            // 2. Vérifier statut actif
            if (agent.getStatut() !=
                    StatutActeur.ACTIF) {
                throw new RuntimeException(
                        "Compte agent inactif");
            }

            // 3. Supprimer anciens OTPs
            otpRepository.deleteByTelephone(telephone);

            // 4. Générer OTP
            String otpClair = String.valueOf(
                    100000 + new Random().nextInt(900000));

            // 5. Sauvegarder OTP hashé
            OtpCode otp = new OtpCode();
            otp.setTelephone(telephone);
            otp.setCode(
                    passwordEncoder.encode(otpClair));
            otp.setActeurId(agent.getId());
            otp.setExpireAt(
                    LocalDateTime.now().plusMinutes(2));
            otpRepository.save(otp);

            // 6. Envoyer OTP par SMS
            smsService.envoyerOtp(telephone, otpClair);

            return Map.of(
                    "message",
                    "Code OTP de cet Agent est  envoyé au " + telephone,
                    "telephone", telephone
            );
    }

    // ═══════════════════════════════════════
// LOGIN ADMIN — sans OTP (pour dashboard React)
// ═══════════════════════════════════════
    @Transactional
    public AuthResponse loginAdmin(AuthRequest req) {
        // 1. Chercher par email
        var acteur = acteurRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        // 2. Vérifier que c'est bien un admin
        if (acteur.getRole() != RoleUtilisateur.ADMIN) {
            throw new RuntimeException("Accès refusé : compte non administrateur");
        }

        // 3. Vérifier statut
        if (acteur.getStatut() != StatutActeur.ACTIF) {
            throw new RuntimeException("Compte administrateur inactif");
        }

        // 4. Vérifier mot de passe
        if (!passwordEncoder.matches(req.getMotDePasse(), acteur.getMotDePasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // 5. Retourner JWT directement (pas d'OTP)
        return new AuthResponse(
                jwtUtils.genererToken(acteur.getId(), acteur.getRole().name()),
                acteur.getRole().name(),
                acteur.getId(),
                acteur.getNom(),
                acteur.getPrenom(),
                acteur.getEmail(),
                ""
        );
    }


//    public AuthResponse login(AuthRequest req){
//        var acteur = acteurRepository
//                .findByEmail(req.getEmail())
//                .orElseThrow(()-> new RuntimeException("Email introuvable"));
//
//        if (!passwordEncoder.matches(
//                req.getMotDePasse(), acteur.getMotDePasse())){
//            throw new RuntimeException("Mot de passe incorrect");
//        }
//
//        //  : récupérer l'adresse selon le type
//        String adresse = "";
//        if (acteur instanceof Client client) {
//            adresse = client.getAdressePrincipale() != null
//                    ? client.getAdressePrincipale() : "";
//        }
//        return new AuthResponse(
//                jwtUtils.genererToken(acteur.getId(), acteur.getRole().name()),
//                acteur.getRole().name(),
//                acteur.getId(),
//                acteur.getNom(),
//                acteur.getPrenom(),
//                acteur.getEmail(),
//                adresse
//        );
//
//    }

}
