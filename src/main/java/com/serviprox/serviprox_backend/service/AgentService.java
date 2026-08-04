package com.serviprox.serviprox_backend.service;

import com.serviprox.serviprox_backend.dto.AgentDTO;
import com.serviprox.serviprox_backend.entity.Agent;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.AgentRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;
    private final PrestataireRepository prestataireRepository;

    // ═══ CREATE ═══
    // Seul le prestataire peut créer un agent
    // L'agent ne peut pas s'inscrire lui-même

    public AgentDTO creerAgent(AgentDTO dto, Long prestataireId){

        Prestataire prestataire = prestataireRepository
                .findById(prestataireId)
                .orElseThrow(()-> new RuntimeException("Prestataire introuvable"));

        Agent agent= new Agent();
        agent.setNom(dto.getNom());
        agent.setPrenom(dto.getPrenom());
        agent.setEmail(dto.getEmail());
        agent.setTelephone(dto.getTelephone());
        agent.setMotoMatricule(dto.getMotoMatricule());
        agent.setCompetences(dto.getCompetences());
        agent.setDisponibilite(true);
        agent.setPrestataire(prestataire);
        agent.setRole(RoleUtilisateur.LIVREUR);
        // Statut Actif par defaut
        agent.setStatut(StatutActeur.ACTIF);

        return  toDTO(agentRepository.save(agent));
    }

    // ═══ READ ALL-Agents ═══

    public List<AgentDTO> getAll(){
        return  agentRepository
                .findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ BY ID ═══

    public AgentDTO getById(Long id){

        return toDTO(agentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Agent introuvable ")));

    }

    // ═══ READ BY PRESTATAIRE ═══
    // Un prestataire voit uniquement ses agents

    public  List<AgentDTO> getByPrestataire(Long prestataireId){
        return  agentRepository
                .findByPrestataireId(prestataireId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ READ DISPONIBLES ═══
    // Pour affecter un agent à une commande
    // on cherche uniquement les disponibles

    public List<AgentDTO> getAgentDisponible(){
        return  agentRepository
                .findByDisponibiliteTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══ UPDATE ═══
    // Mise à jour des infos de l'agent
    public AgentDTO modifier(Long id, AgentDTO dto){
        Agent agent= agentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));

        if (dto.getNom()!=null)
            agent.setNom(dto.getNom());
        if (dto.getPrenom()!= null)
            agent.setPrenom(dto.getPrenom());
        if (dto.getTelephone()!=null)
            agent.setTelephone(dto.getTelephone());
        if (dto.getMotoMatricule()!=null)
            agent.setMotoMatricule(dto.getMotoMatricule());
        if (dto.getCompetences()!=null)
            agent.setCompetences(dto.getCompetences());
        return toDTO(agentRepository.save(agent));

    }

    // ═══ UPDATE DISPONIBILITE ═══
    // true  → agent disponible pour mission
    // false → agent en mission, non disponible

    public AgentDTO actualiserDisponibilites( Long id, Boolean disponible){

        Agent agent = agentRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));

        agent.setDisponibilite(disponible);
        return toDTO(agentRepository.save(agent));

    }

    // ═══ UPDATE POSITION GPS ═══
    // Appelé pendant le déplacement
    // Flutter affiche position en temps réel au client
    // Format : "14.7167,-17.4677" (lat,lng Dakar)

    public AgentDTO actualiserPositionGPS(Long id, String position){

        Agent agent = agentRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));
        agent.setPositionActuelle(position);
        return toDTO(agentRepository.save(agent));
    }


    // ═══ DÉSACTIVER ═══
    // On ne supprime jamais un agent
    // Ses affectations et historique sont conservés

    public  AgentDTO desactiverAgent(Long id){
        Agent agent = agentRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));
       agent.setStatut(StatutActeur.INACTIF);

        return toDTO(agentRepository.save(agent));
    }

    // ═══ RÉACTIVER ═══
    // L'agent revient sur la plateforme

    public  AgentDTO reactiverAgent(Long id){
        Agent agent = agentRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Agent Introuvable"));
        agent.setStatut(StatutActeur.ACTIF);
        agent.setDisponibilite(true);
        return toDTO(agentRepository.save(agent));
    }

    // ═══ GET POSITION ACTUELLE ═══
    // Retourne la position GPS actuelle
    // de l'agent — appelée par GPSController
    // Flutter client interroge toutes les 5s
    // pour afficher sur Google Maps
    public String getPositionActuelle( Long agentId) {
        Agent agent = agentRepository
                .findById(agentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Agent introuvable"));
        return agent.getPositionActuelle();
    }


    // ═══ MAPPER entité → DTO ═══
    public  AgentDTO toDTO(Agent a){
        AgentDTO dto = new AgentDTO();
        dto.setId(a.getId());
        dto.setNom(a.getNom());
        dto.setPrenom(a.getPrenom());
        dto.setEmail(a.getEmail());
        dto.setTelephone(a.getTelephone());
        dto.setMotoMatricule(a.getMotoMatricule());
        dto.setCompetences(a.getCompetences());
        dto.setDisponibilite(a.getDisponibilite());
        dto.setPositionActuelle(a.getPositionActuelle());
        dto.setStatut(a.getStatut() != null ? a.getStatut().name():null);
        if (a.getPrestataire() !=null)
            dto.setPrestataireId(a.getPrestataire().getId());

        return dto;
    }


}
