package com.serviprox.serviprox_backend;

import com.serviprox.serviprox_backend.dto.AgentDTO;
import com.serviprox.serviprox_backend.entity.Agent;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.AgentRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import com.serviprox.serviprox_backend.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {
    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PrestataireRepository prestataireRepository;

    @InjectMocks
    private AgentService agentService;
    private Prestataire prestataire;

    private Agent agent;


    @BeforeEach
    void setUp(){
        prestataire = new Prestataire();
        prestataire.setId(1L);
        prestataire.setNomCommercial("Abds-Pressing Mermoz");

        agent = new Agent();
        agent.setId(1L);
        agent.setNom("Diop");
        agent.setPrenom("Abdou");
        agent.setEmail("abdou@test.com");
        agent.setTelephone("771234567");
        agent.setMotoMatricule("DK-1234");
        agent.setDisponibilite(true);
        agent.setPrestataire(prestataire);
        agent.setStatut(StatutActeur.ACTIF);

    }
    // ═══ TEST 1 ═══
    // Créer un agent disponible
    @Test
    void creerAgentDisponibleTrue(){
        //GIVEN
        AgentDTO dto = new AgentDTO();
        dto.setNom("Diop");
        dto.setPrenom("Abdou");
        dto.setEmail("abdou@test.com");
        dto.setTelephone("771234567");
        dto.setMotoMatricule("DK-1234");

        when(prestataireRepository.findById(1l)).thenReturn(Optional.of(prestataire));
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        //WHEN
        AgentDTO result = agentService.creerAgent(dto,1L);
        assertNotNull(result);
        assertEquals("Diop", result.getNom());
        verify(agentRepository,times(1)).save(any(Agent.class));
    }

    // ═══ TEST 2 ═══
    // Agents disponibles uniquement
    @Test
    void getDisponibilesDoitRetournerDisponibles(){
        //GIVEN
        when(agentRepository.findByDisponibiliteTrue()).thenReturn(List.of(agent));

        //WHEN
        List<AgentDTO> result = agentService.getAgentDisponible();

        //THEN
        assertEquals(1,result.size());
        assertTrue(result.get(0).getDisponibilite());
    }

    // ═══ TEST 3 ═══
    // Changer disponibilité
    @Test
    void changerDisponibilieDoitMettreAjour(){
        //GIVEN
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any())).thenReturn(agent);

        //WHEN
        AgentDTO result = agentService.actualiserDisponibilites(1L,false);

        //THEN
        assertNotNull(result);
        verify(agentRepository,times(1)).save(any(Agent.class));
    }
    // ═══ TEST 4 ═══
    // Mettre à jour position GPS
    @Test
    void mettreAjourPositionDoitSauvegarderGPS(){
        //GIVEN
        String position = "14.7167,-17.4677";
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any())).thenReturn(agent);

        //WHEN
        AgentDTO result = agentService.actualiserPositionGPS(1L,position);

        //THEN
        assertNotNull(result);
        verify(agentRepository,times(1)).save(any(Agent.class));
    }

    // ═══ TEST 5 ═══
    // Agent prestataire introuvable
    @Test
    void creerAgentPrestataireInexistantDoitEchouer(){
        //GIVEN
        AgentDTO dto = new AgentDTO();
        when(prestataireRepository.findById(99L)).thenReturn(Optional.empty());

        //WHEN +THEN
        assertThrows(RuntimeException.class,()->agentService.creerAgent(dto,99L));
    }



}
