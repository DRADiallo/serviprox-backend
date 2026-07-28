package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.ClientDTO;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private  final ClientRepository clientRepository;

    // ═══ CREATE ═══
    // Utilisé par AuthService lors de l'inscription
    // On garde léger ici — AuthService gère le mdp

    public ClientDTO creer (Client client){
        client.setStatut(StatutActeur.ACTIF);
        return toDTO(clientRepository.save(client));

    }
    // ═══ READ ALL ═══
    // Utilisé par AdminService pour supervision

    public List<ClientDTO>getAll(){
        return  clientRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }

    public ClientDTO getById(Long id){
        return toDTO(clientRepository
                .findById(id)
                .orElseThrow(()->new RuntimeException("Client Introuvable")));
    }

    // ═══ UPDATE ═══
    // Le client met à jour son profil
    // depuis l'application Flutter
    public ClientDTO  modifier(Long id, ClientDTO dto){
        Client client= clientRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Client Introuvable"));
        // On ne modifie que les champs autorisés
        // Email et mot de passe ont leur propre endpoint
        if (dto.getNom()!=null)
            client.setNom(dto.getNom());
        if (dto.getPrenom()!=null)
            client.setPrenom(dto.getPrenom());
        if (dto.getTelephone()!=null)
            client.setTelephone(dto.getTelephone());
        if (dto.getAdresse() !=null)
            client.setAdressePrincipale(dto.getAdresse());

        return toDTO(clientRepository.save(client));

    }

    // ═══ DÉSACTIVER  ═══
    // On ne supprime jamais un client
    // Son historique de commandes est conservé
    // Cas : client signalé pour comportement abusif

    public ClientDTO desactiver(Long id){
        Client c= clientRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Client Introuvable"));
        c.setStatut(StatutActeur.INACTIF);
        return toDTO(clientRepository.save(c));
    }

    // ═══ RÉACTIVER ═══
    // Après vérification du compte


    public ClientDTO reactiver(Long id){
        Client c= clientRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Client Introuvable"));
        c.setStatut(StatutActeur.ACTIF);
        return toDTO(clientRepository.save(c));
    }

    // ═══ MAPPER entité → DTO ═══
    public ClientDTO toDTO(Client c){
        ClientDTO clientDTO= new ClientDTO();
        clientDTO.setId(c.getId());
        clientDTO.setNom(c.getNom());
        clientDTO.setPrenom(c.getPrenom());
        clientDTO.setEmail(c.getEmail());
        clientDTO.setTelephone(c.getTelephone());
        clientDTO.setAdresse(c.getAdressePrincipale());
        clientDTO.setStatut(c.getStatut()!=null?c.getStatut().name():null);

        return clientDTO;
    }

}
