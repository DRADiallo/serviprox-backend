package com.serviprox.serviprox_backend.service;
import com.serviprox.serviprox_backend.dto.CreneauDTO;
import com.serviprox.serviprox_backend.entity.Creneau;
import com.serviprox.serviprox_backend.enums.StatutCreneau;
import com.serviprox.serviprox_backend.repository.CreneauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public  class CreneauService {
    private final CreneauRepository creneauRepository;
    private final NotificationService notificationService;

    // ═══ CREATE ═══
    // Créer un créneau pour une intervention
    //
    // Contexte sénégalais — Soins à domicile :
    // Le client DOIT choisir une date, une heure
    // et une durée estimée AVANT de valider
    // sa réservation. C'est OBLIGATOIRE.
    //
    // Exemples :
    // Coiffure     : Samedi 10h → 12h
    // Ménage       : Lundi 8h → 11h
    // Vidange fosse: Mercredi 9h → 10h

    public CreneauDTO creerCreneau(LocalDateTime dateDebut, LocalDateTime dateFin){

        // La date doit être dans le futur
        // On ne peut pas réserver dans le passé !
        if (dateDebut.isBefore(LocalDateTime.now())){
            throw new RuntimeException("La date du créneau doit être " + " dans le futur");
        }

        // Vérifier durée minimale — 30 minutes
        // Une prestation ne peut pas durer
        // moins de 30 minutes
         if (dateDebut.plusMinutes(30).isAfter(dateFin)){
             throw  new RuntimeException("La durée minimale est de 30 minutes ");

         }

        // Créer le créneau avec statut LIBRE
        // LIBRE = disponible, pas encore réservé
        Creneau creneau = new Creneau();
         creneau.setDateDebut(dateDebut);
         creneau.setDateFin(dateFin);
         creneau.setStatut(StatutCreneau.LIBRE);

         return  toDTO(creneauRepository.save(creneau));

    }

    // ═══ READ ALL ═══
    // Tous les créneaux — admin uniquement
    public CreneauDTO getById(Long id){
        return toDTO(creneauRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Créneau introuvable")));
    }

    // ═══ READ DISPONIBLES ═══
    // Flutter affiche ces créneaux au client
    // pour qu'il choisisse son horaire
    // Uniquement les créneaux LIBRES
     public List<CreneauDTO> getDisponibles(){

        return  creneauRepository
                .findByStatut(StatutCreneau.LIBRE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
     }

    // ═══ READ RÉSERVÉS ═══
    // Créneaux déjà pris
    // Utile pour afficher le calendrier
    // du prestataire dans Flutter

    public List<CreneauDTO>getReserves(){

        return creneauRepository
                .findByStatut(StatutCreneau.RESERVE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ UPDATE — RÉSERVER ═══
    // Quand le client confirme sa réservation
    // LIBRE → RESERVE
    // Évite les doubles réservations
    // Ex: deux clients ne peuvent pas réserver
    // le même créneau de coiffure

    public CreneauDTO reserverCreneau(Long id){

        Creneau creneau = creneauRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Ce créneau Introuvable "));

        //veuillez choisir un autre horaire
        if (creneau.getStatut()== StatutCreneau.RESERVE){
            throw new RuntimeException("Ce créneau est déjà réservé !! "+ "veuillez choisir un autre horaire");
        }
        creneau.setStatut(StatutCreneau.RESERVE);
        return toDTO(creneauRepository.save(creneau));
    }

    // ═══ DELETE ═══
    // Supprimer un créneau
    // Uniquement si LIBRE
    // On ne supprime pas un créneau réservé !

    public void supprimer(Long id){

        Creneau creneau= creneauRepository
                .findById(id)
                .orElseThrow(()->  new RuntimeException("Creneau Intouvable"));

        // On ne peut pas supprimer
        // un créneau déjà réservé
        if (creneau.getStatut()== StatutCreneau.RESERVE){

            throw new RuntimeException("Impossible de supprimer"+ "un créneau réservé");
        }
        creneauRepository.deleteById(id);
    }

    // ═══ MAPPER entité → DTO ═══
    public CreneauDTO toDTO(Creneau c){

        CreneauDTO dto = new CreneauDTO();
        dto.setId(c.getId());
        dto.setDateDebut(c.getDateDebut());
        dto.setDateFin(c.getDateFin());
        dto.setStatut(c.getStatut() !=null ? c.getStatut().name():null);

        return dto;
    }
}
