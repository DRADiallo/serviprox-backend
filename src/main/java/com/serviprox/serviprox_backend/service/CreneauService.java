package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Creneau;
import com.serviprox.serviprox_backend.enums.StatutCreneau;
import com.serviprox.serviprox_backend.repository.CreneauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreneauService {
    private final CreneauRepository creneauRepository;
    private final NotificationService notificationService;

    /**
     * Créons un créneau pour une intervention
     *
     * Contexte sénégalais — Soins à domicile :
     * Le client DOIT choisir une date, une heure
     * et une durée estimée AVANT de valider
     * sa réservation. C'est obligatoire.
     *
     * Exemples :
     * - Coiffure : Samedi 10h, durée 2h
     * - Ménage   : Lundi 8h, durée 3h
     * - Vidange fosse : Mercredi 9h, durée 1h
     */

    public Creneau creerCreneau(LocalDateTime dateDebut, LocalDateTime dateFin) {

        // Vérifions que la date est dans le futur
        // On ne peut pas réserver dans le passé !

        if (dateDebut.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("\"La date du créneau doit être \"\n" + "   + \"dans le futur\"");
        }

        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("La date de fin doit être après \"\n" + "   + \"la date de début");
        }

        // Creeons creneau avec un statut libre
        Creneau creneau = new Creneau();
        creneau.setDateDebut(dateDebut);
        creneau.setDateFin(dateFin);
        creneau.setStatut(StatutCreneau.LIBRE);
        return creneauRepository.save(creneau);
    }

        /**
         * Réservons un créneau
         *
         * Quand le client confirme sa réservation,
         * le créneau passe de LIBRE à RESERVE
         * pour éviter les doubles réservations
         */
    public Creneau reserverCreneau(Long creneauId){

        Creneau creneau = creneauRepository
                .findById(creneauId)
                .orElseThrow(()-> new RuntimeException("Creneau introuvable"));

        // // Vérifions que le créneau n'est pas déjà réservé

        if (creneau.getStatut()== StatutCreneau.RESERVE){
            throw new RuntimeException("Ce créneau est déjà réservé. \"\n" + " + \"Veuillez choisir un autre horaire.");
        }

        // Marquer comme réservé

        creneau.setStatut(StatutCreneau.RESERVE);
        return creneauRepository.save(creneau);


    }

    /**
     * Libérons un créneau
     *
     * Si la commande est annulée,
     * le créneau redevient disponible
     * pour d'autres clients
     */
    public Creneau linbererCreneau(Long creneauId){

        Creneau creneau = creneauRepository
                .findById(creneauId)
                .orElseThrow(()->new RuntimeException("Creneau intouvable"));

        // Remmettons le creneau disponible afin que les autres clients puissent s en beneficier

        creneau.setStatut(StatutCreneau.LIBRE);
        return creneauRepository.save(creneau);

    }

    // Creneau disponibles a afficher !!!!
    public List<Creneau>getCreneauxDisponibles(){
        return  creneauRepository.findByStatut(StatutCreneau.LIBRE);
    }



}
