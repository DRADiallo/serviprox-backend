package com.serviprox.serviprox_backend.controller;

import com.serviprox.serviprox_backend.dto.AuthRequest;
import com.serviprox.serviprox_backend.dto.AuthResponse;
import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
import com.serviprox.serviprox_backend.dto.RegisterPrestataireRequest;
import com.serviprox.serviprox_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ═══════════════════════════════════════
    // POST /api/auth/register/client
    // ═══

    // @PostMapping → requête HTTP POST
    // Utilisé pour CRÉER une ressource
    //
    // Qui appelle cet endpoint ?
    // → Flutter (écran inscription client)
    //
    // Ce que Flutter envoie (JSON) :
    // {
    //   "nom": "Diallo",
    //   "prenom": "Awa",
    //   "email": "awa@test.com",
    //   "telephone": "771234567",
    //   "adressePrincipale": "Médina, Dakar",
    //   "motDePasse": "Test1234"
    // }
    //
    // @RequestBody → Spring convertit
    // automatiquement le JSON reçu
    // en objet RegisterClientRequest
    //
    // Ce que Flutter reçoit en retour :
    // {
    //   "token": "eyJhbGciOiJIUzI1NiJ9...",
    //   "role": "CLIENT",
    //   "userId": 1,
    //   "nom": "Diallo",
    //   "prenom": "Awa",
    //   "email": "awa@test.com"
    // }
    //
    // ResponseEntity.ok() → HTTP 200 OK
    // + le résultat en JSON
    @PostMapping("/register/client")
    public ResponseEntity<AuthResponse>registerClient(@RequestBody RegisterClientRequest req){

        return ResponseEntity.ok(authService.registerClient(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/register/prestataire
    // ═══════════════════════════════════════
    //
    // Différence avec /register/client :
    // → Prestataire créé avec statut
    //   EN_ATTENTE_VALIDATION
    // → Admin doit valider avant accès
    @PostMapping("/register/prestataire")
    public ResponseEntity<AuthResponse>registerPrestataire(@RequestBody RegisterPrestataireRequest req){

        return ResponseEntity.ok(authService.registerPrestataire(req));
    }

    // ═══════════════════════════════════════
    // POST /api/auth/login
    // ═══════════════════════════════════════
    //
    // Qui appelle cet endpoint ?
    // → Flutter (écran connexion)
    // → React Admin (connexion admin)
    //
    // Ce que Flutter envoie :
    // {
    //   "email": "awa@test.com",
    //   "motDePasse": "Test1234"
    // }
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
    //   CLIENT      → PageAccueilClient
    //   PRESTATAIRE → TableauBordPrestataire
    //   LIVREUR     → EcranAgent
    //   ADMIN       → Dashboard React
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req){

        return ResponseEntity.ok( authService.login(req));
    }


}
