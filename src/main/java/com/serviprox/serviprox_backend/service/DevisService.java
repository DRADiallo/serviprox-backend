package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Devis;
import com.serviprox.serviprox_backend.enums.StatutDevis;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.DevisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevisService {
    private final DevisRepository devisRepository;
    private final CommandeRepository commandeRepository;

    //Creer un devis pur une commande
    public Devis creerDevis(Long commandeId, Double montantEstime){
        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));

        Devis devis= new Devis();
        devis.setCommande(commande);
        devis.setMontantEstime(montantEstime);
        devis.setStatut(StatutDevis.EN_ATTENTE);
        return devisRepository.save(devis);

    }

    // Valider un devis
    public  Devis validerDevis(Long devisId){

        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(()-> new RuntimeException("Devis introuvable "));
        devis.setStatut(StatutDevis.VALIDE);
        return devisRepository.save(devis);
    }

    // Refuser un devis
    public Devis refuserDevis(Long devisId){
        Devis devis= devisRepository.findById(devisId)
                .orElseThrow(()->new RuntimeException("Devis introuvable"));
        devis.setStatut(StatutDevis.REFUSE);
        return devisRepository.save(devis);
    }

    // Devis par Commande
    public Devis getByCommande(Long commandeId){
        return  devisRepository
                .findByCommandeId(commandeId)
                .orElseThrow(()-> new RuntimeException("Devis introuvable"));

    }
}
