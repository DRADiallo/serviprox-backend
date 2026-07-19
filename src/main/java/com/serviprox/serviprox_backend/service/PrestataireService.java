package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.PrestataireDTO;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestataireService {
     private final PrestataireRepository prestataireRepository;

    // ═══ CREATE ═══
    // Utilisé par AuthService lors de l'inscription
    public PrestataireDTO creer(Prestataire p){
        p.setStatut(StatutActeur.EN_ATTENTE_VALIDATION);
        return  toDTO(prestataireRepository.save(p));

    }

    // ═══ lister tous les prestataires ═══
    public  List<PrestataireDTO>getAll(){
        return prestataireRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }
    // ═══ READ ACTIFS ═══
    // Seuls les prestataires ACTIFS
    // sont visibles pour les clients dans l interface  Flutter

    public List<PrestataireDTO>getAllActifs(){
        return  prestataireRepository
                .findByStatut(StatutActeur.ACTIF)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

     }

    // ═══ READ Prestataire BY ID ═══
    public PrestataireDTO getById(Long id){
        Prestataire p = prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Prestataire introuvable"));
        return toDTO(p);
    }

    // ═══ READ BY ZONE ═══
    // Recherche par quartier
    // Ex: "Médina", "Plateau", "Almadies"

    public List<PrestataireDTO>getByZone(String zone){
        return prestataireRepository
                .findByZoneCouverture(zone)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ UPDATE ═══
    // Le prestataire met à jour ses informations
    // depuis son back-office Flutter

    public PrestataireDTO modifier(Long id, PrestataireDTO prestataireDTO){
        Prestataire p= prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Prestataire introuvable"));

        // On ne modifie que les champs autorisés
        // Le statut et le mot de passe ne changent pas ici
        if (prestataireDTO.getNomCommercial() != null)
            p.setNomCommercial(prestataireDTO.getNomCommercial());
        if (prestataireDTO.getZone() != null)
            p.setZoneCouverture(prestataireDTO.getZone());

        return  toDTO(prestataireRepository.save(p));

    }

    // ═══ UPDATE STATUT ═══
    // Utilisé par AdminService uniquement
    public  PrestataireDTO changerStaut(Long id, StatutActeur statutActeur){
        Prestataire p = prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Prestataire introuvable"));
        p.setStatut(statutActeur);
        return toDTO(prestataireRepository.save(p));

    }
    // ═══ DESACTIVER  ═══
    // On préfère suspendre plutôt que supprimer

     public  PrestataireDTO desactiver(Long id , String motif){

        Prestataire p = prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));
        p.setStatut(StatutActeur.INACTIF);
        prestataireRepository.save(p);
        return toDTO(p);
     }

    // ═══ SUSPENDRE ═══
    public PrestataireDTO suspendre(Long id){
        Prestataire p = prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException(" Prestataire Introuvable "));
        p.setStatut(StatutActeur.SUSPENDU);
        prestataireRepository.save(p);
        return toDTO(p);

    }

    // ═══ RÉACTIVER ═══
    public  PrestataireDTO reactiver(Long id){
        Prestataire p = prestataireRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Prestataire Introuvable"));
        p.setStatut(StatutActeur.ACTIF);
        prestataireRepository.save(p);
        return  toDTO(p);
    }

    // ═══ MAPPER entité → DTO ═══
    private PrestataireDTO toDTO(Prestataire p){
        PrestataireDTO dto = new PrestataireDTO();
        dto.setId(p.getId());
        dto.setNomCommercial(p.getNomCommercial());
        dto.setZone(p.getZoneCouverture());
        dto.setNoteMoyenne(p.getNoteMoyenne());
        dto.setOuvert(p.getStatut()== StatutActeur.ACTIF);
        return  dto;
    }



}
