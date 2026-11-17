package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.AuthRequest;
import com.serviprox.serviprox_backend.dto.AuthResponse;
import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
import com.serviprox.serviprox_backend.dto.RegisterPrestataireRequest;
import com.serviprox.serviprox_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // POST /api/auth/login/admin
    @PostMapping("/login/admin")
    public ResponseEntity<AuthResponse> loginAdmin(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.loginAdmin(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/register/client
    // ═══════════════════════════════════════
    @PostMapping("/register/client")
    public ResponseEntity<AuthResponse>
    registerClient(
            @RequestBody
            RegisterClientRequest req) {
        return ResponseEntity.ok(
                authService.registerClient(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/register/prestataire
    // ═══════════════════════════════════════
    @PostMapping("/register/prestataire")
    public ResponseEntity<AuthResponse>
    registerPrestataire(
            @RequestBody
            RegisterPrestataireRequest req) {
        return ResponseEntity.ok(
                authService.registerPrestataire(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/login — ÉTAPE 1
    // ───────────────────────────────────────
    // Qui appelle cet endpoint ?
    // → Flutter (écran connexion)
    //
    // Ce que Flutter envoie :
    // {
    //   "email": "awa@test.com",
    //   "motDePasse": "Test1234",
    //   "telephone": "771234567" ← AJOUT
    // }
    //
    // Ce que Flutter reçoit :
    // {
    //   "message": "Code OTP envoyé au 771234567",
    //   "telephone": "771234567"
    // }
    //
    // ← CHANGEMENT : retourne Map au lieu
    // de AuthResponse directement
    // Le token JWT sera retourné à l'étape 2
    // ═══════════════════════════════════════
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>>
    login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(
                authService.login(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/verify-otp — ÉTAPE 2
    // ───────────────────────────────────────
    // Qui appelle cet endpoint ?
    // → Flutter (écran OTP)
    //
    // Ce que Flutter envoie :
    // telephone=771234567&code=482917
    //
    // Ce que Flutter reçoit :
    // {
    //   "token": "eyJhbGciOiJIUzI1NiJ9...",
    //   "role": "CLIENT",
    //   "userId": 1,
    //   "nom": "Diallo",
    //   "prenom": "Awa",
    //   "email": "awa@test.com"
    // }
    //
    // Flutter utilise ensuite :
    // → token → envoyé dans chaque requête
    // → role  → affiche bon écran
    //   CLIENT      → HomeScreen
    //   PRESTATAIRE → PrestataireHomeScreen
    //   LIVREUR     → AgentHomeScreen
    //   ADMIN       → Message "web uniquement"
    // ═══════════════════════════════════════
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String telephone,
            @RequestParam String code) {
        try {
            return ResponseEntity.ok(
                    authService.verifyOtp(telephone, code));
        } catch (RuntimeException e) {
            // Retourne 400 + {"message": "..."} lisible par Flutter
            // Ex : "Compte en attente de validation par l'administrateur"
            //      "Code OTP expiré"
            //      "Code OTP incorrect"
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════
    // POST /api/auth/resend-otp
    // ───────────────────────────────────────
    // Si l'OTP expire ou pas reçu
    // → Renvoyer un nouveau code
    //
    // Ce que Flutter envoie :
    // telephone=771234567
    //
    // Ce que Flutter reçoit :
    // {
    //   "message": "Nouveau code envoyé au 771234567"
    // }
    // ═══════════════════════════════════════
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>>
    resendOtp(
            @RequestParam String telephone) {
        return ResponseEntity.ok(
                authService.resendOtp(telephone));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/login/agent
    // ───────────────────────────────────────
    // Login spécifique pour les agents
    // Pas d'email — juste téléphone + PIN
    //
    // Ce que Flutter envoie :
    // telephone=771234567&codePin=482917
    //
    // Ce que Flutter reçoit :
    // {
    //   "token": "eyJhbGciOiJIUzI1NiJ9...",
    //   "role": "LIVREUR",
    //   "userId": 5,
    //   "nom": "Seck",
    //   "prenom": "Moussa"
    // }
    // ═══════════════════════════════════════
    @PostMapping("/login/agent")
    public ResponseEntity<Map<String, String>>
    loginAgent(
            @RequestParam String telephone) {
        return ResponseEntity.ok(
                authService.loginAgent(telephone));
    }
}









//package com.serviprox.serviprox_backend.controller;
//
//import com.serviprox.serviprox_backend.dto.AuthRequest;
//import com.serviprox.serviprox_backend.dto.AuthResponse;
//import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
//import com.serviprox.serviprox_backend.dto.RegisterPrestataireRequest;
//import com.serviprox.serviprox_backend.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//
//    // ═══════════════════════════════════════
//    // POST /api/auth/register/client
//    // ═══
//
//    // @PostMapping → requête HTTP POST
//    // Utilisé pour CRÉER une ressource
//    //
//    // Qui appelle cet endpoint ?
//    // → Flutter (écran inscription client)
//    //
//    // Ce que Flutter envoie (JSON) :
//    // {
//    //   "nom": "Diallo",
//    //   "prenom": "Awa",
//    //   "email": "awa@test.com",
//    //   "telephone": "771234567",
//    //   "adressePrincipale": "Médina, Dakar",
//    //   "motDePasse": "Test1234"
//    // }
//    //
//    // @RequestBody → Spring convertit
//    // automatiquement le JSON reçu
//    // en objet RegisterClientRequest
//    //
//    // Ce que Flutter reçoit en retour :
//    // {
//    //   "token": "eyJhbGciOiJIUzI1NiJ9...",
//    //   "role": "CLIENT",
//    //   "userId": 1,
//    //   "nom": "Diallo",
//    //   "prenom": "Awa",
//    //   "email": "awa@test.com"
//    // }
//    //
//    // ResponseEntity.ok() → HTTP 200 OK
//    // + le résultat en JSON
//    @PostMapping("/register/client")
//    public ResponseEntity<AuthResponse>registerClient(@RequestBody RegisterClientRequest req){
//
//        return ResponseEntity.ok(authService.registerClient(req));
//    }
//
//    // ═══════════════════════════════════════
//    // POST /api/auth/register/prestataire
//    // ═══════════════════════════════════════
//    //
//    // Différence avec /register/client :
//    // → Prestataire créé avec statut
//    //   EN_ATTENTE_VALIDATION
//    // → Admin doit valider avant accès
//    @PostMapping("/register/prestataire")
//    public ResponseEntity<AuthResponse>registerPrestataire(@RequestBody RegisterPrestataireRequest req){
//
//        return ResponseEntity.ok(authService.registerPrestataire(req));
//    }
//
//    // ═══════════════════════════════════════
//    // POST /api/auth/login
//    // ═══════════════════════════════════════
//    //
//    // Qui appelle cet endpoint ?
//    // → Flutter (écran connexion)
//    // → React Admin (connexion admin)
//    //
//    // Ce que Flutter envoie :
//    // {
//    //   "email": "awa@test.com",
//    //   "motDePasse": "Test1234"
//    // }
//    //
//    // Ce que Flutter reçoit :
//    // {
//    //   "token": "eyJhbGciOiJIUzI1NiJ9...",
//    //   "role": "CLIENT",
//    //   "userId": 1,
//    //   "nom": "Diallo",
//    //   "prenom": "Awa",
//    //   "email": "awa@test.com"
//    // }
//    //
//    // Flutter utilise ensuite :
//    // → token → envoyé dans chaque requête
//    // → role  → affiche bon écran
//    //   CLIENT      → PageAccueilClient
//    //   PRESTATAIRE → TableauBordPrestataire
//    //   LIVREUR     → EcranAgent
//    //   ADMIN       → Dashboard React
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req){
//
//        return ResponseEntity.ok( authService.login(req));
//    }
//
//
//}