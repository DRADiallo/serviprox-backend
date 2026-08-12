package com.serviprox.serviprox_backend;

import com.serviprox.serviprox_backend.dto.CommandeDTO;
import com.serviprox.serviprox_backend.dto.CommandeRequest;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.entity.Commande;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.enums.StatutCommande;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.CommandeRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import com.serviprox.serviprox_backend.service.CommandeService;
import com.serviprox.serviprox_backend.service.EvenementSuiviService;
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
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommandeServiceTest {

    @Mock
    private CommandeRepository commandeRepository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    private PrestataireRepository prestataireRepository;
    @Mock
    private EvenementSuiviService evenementSuiviService;
    @InjectMocks
    private CommandeService commandeService;

    private Client client;
    private Prestataire prestataire;
    private Commande commande;

    @BeforeEach
    void setUp(){
       client = new Client();
       client.setId(1L);
       client.setNom("Diallo");
       client.setPrenom("Abdourahamne");
       client.setEmail("abdou@test.com");


       prestataire =new Prestataire();
       prestataire.setId(2L);
       prestataire.setNomCommercial("Maya-Pressing");
       prestataire.setStatut(StatutActeur.ACTIF);

       commande = new Commande();
       commande.setId(1L);
       commande.setClient(client);
       commande.setPrestataire(prestataire);
       commande.setStatutCommande(StatutCommande.CREE);
       commande.setMontantTotal(0.0);
    }

    // ═══ TEST 1 ═══
    // Créer une commande avec client et prestataire valides
    @Test
    void creerCommandeClientEtPrestataireVlidesDoitReussir(){
        // GIVEN
        CommandeRequest request= new CommandeRequest();
        request.setPrestataireId(2L);
        request.setModeDepot("COLLECTE_DOMICILE");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(prestataireRepository.findById(2L)).thenReturn(Optional.of(prestataire));
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // WHEN
        CommandeDTO result =commandeService.creerCommande(1L,request);

        //THEN
       assertNotNull(result);
       assertEquals("CREE",result.getStatut());
       verify(commandeRepository, times(1)).save(any(Commande.class));

    }

    // ═══ TEST 2 ═══
    // Client introuvable → exception
    @Test
    void creerCommandeClientIntrouvableVlidesDoitEchouer(){

        // GIVEN
        CommandeRequest request= new CommandeRequest();
        request.setPrestataireId(2L);

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        //WHEN + THEN

        assertThrows(RuntimeException.class,()-> commandeService.creerCommande(99L, request));
        verify(commandeRepository, never()).save(any(Commande.class));

    }

    // ═══ TEST 3 ═══
    // Changer statut — transition valide
    // CREE → CONFIRMEE
    @Test
    void changerStatutTransitionValideDoitReussir(){
        // GIVEN
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any())).thenReturn(commande);

        // WHEN
        CommandeDTO result = commandeService.actuliserStatut(1L,"CONFIRMEE",2L);
        //THEN
        assertNotNull(result);
        verify(commandeRepository, times(1)).save(any(Commande.class));

    }

    // ═══ TEST 4 ═══
    // Transition invalide
    // CREE → CLOTUREE impossible !
    @Test
    void changerStatutTransitionInValideDoitEchouer(){
        // GIVEN
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

       // WHEN + THEN
        assertThrows(RuntimeException.class, ()-> commandeService
                .actuliserStatut(1L,"CLOTUREE",2L));

    }

    // ═══ TEST 5 ═══
    // Récupérer commandes d'un client
    @Test
    void getByClientDoitRetournerListeCommandes(){

        // GIVEN
        when(commandeRepository.findByClientId(1L)).thenReturn(List.of(commande));

        lenient().when(evenementSuiviService
                        .enregistrer(any(), any(), any(),
                                any(), any(), any()))
                .thenReturn(null);

        // WHEN
       List<CommandeDTO>result = commandeService.getByClient(1L);

       //THEN
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1,result.size());

    }


}
