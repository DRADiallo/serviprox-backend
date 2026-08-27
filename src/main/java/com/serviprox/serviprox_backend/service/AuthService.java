package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.config.JwtUtils;
import com.serviprox.serviprox_backend.dto.AuthRequest;
import com.serviprox.serviprox_backend.dto.AuthResponse;
import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
import com.serviprox.serviprox_backend.dto.RegisterPrestataireRequest;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.ActeurRepository;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ClientRepository clientRepository;
    private final PrestataireRepository prestataireRepository;
    private final ActeurRepository acteurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


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
                client.getEmail()
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
                p.getEmail()
        );


    }

    public AuthResponse login(AuthRequest req){
        var acteur = acteurRepository
                .findByEmail(req.getEmail())
                .orElseThrow(()-> new RuntimeException("Email introuvable"));

        if (!passwordEncoder.matches(
                req.getMotDePasse(), acteur.getMotDePasse())){
            throw new RuntimeException("Mot de passe incorrect");
        }
        return new AuthResponse(
                jwtUtils.genererToken(acteur.getId(), acteur.getRole().name()),
                acteur.getRole().name(),
                acteur.getId(),
                acteur.getNom(),
                acteur.getPrenom(),
                acteur.getEmail()
        );

    }

}
