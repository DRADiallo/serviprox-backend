package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.entity.Paiement;
import com.serviprox.serviprox_backend.enums.ModePaiement;
import com.serviprox.serviprox_backend.enums.StatutPaiement;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;

    // Creer un paiement
    public Paiement creerPaiement(Long commandeId, Double montant, String modePaiement){
        var commande = commandeRepository
                .findById(commandeId)
                .orElseThrow(()-> new RuntimeException("Commande introuvable"));

        Paiement paiement= new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(montant);
        paiement.setMode(ModePaiement.valueOf(modePaiement));
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        return paiementRepository.save(paiement);
    }

    //Confirmer paiement
    public  Paiement confirmerPaiement(Long paiementId){
        Paiement paiement = paiementRepository
                .findById(paiementId)
                .orElseThrow(()-> new RuntimeException("Paiement introuvable "));
        paiement.setStatut(StatutPaiement.EFFECTUE);
        return paiementRepository.save(paiement);
    }

    // Paiement par commande
    public  Paiement getByCommande(Long commandeId){
        return  paiementRepository
                .findById(commandeId)
                .orElseThrow(()->new RuntimeException(" Paienment Introuvable"));
    }

}
