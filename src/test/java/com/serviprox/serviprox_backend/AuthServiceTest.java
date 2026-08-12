package com.serviprox.serviprox_backend;

import com.serviprox.serviprox_backend.config.JwtUtils;
import com.serviprox.serviprox_backend.dto.AuthRequest;
import com.serviprox.serviprox_backend.dto.AuthResponse;
import com.serviprox.serviprox_backend.dto.RegisterClientRequest;
import com.serviprox.serviprox_backend.entity.Client;
import com.serviprox.serviprox_backend.enums.RoleUtilisateur;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.ActeurRepository;
import com.serviprox.serviprox_backend.repository.ClientRepository;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import com.serviprox.serviprox_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


// @ExtendWith → active Mockito pour JUnit 5
// Mockito = simuler les dépendances
// sans toucher la vraie base de données
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    // @Mock → crée un faux objet simulé
    // On contrôle ce qu'il retourne
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private ActeurRepository acteurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;
    // @InjectMocks → crée le vrai service
    // et injecte tous les @Mock dedans
    @InjectMocks
    private AuthService authService;

    private RegisterClientRequest rClientRequest;
    private Client client;

    // @BeforeEach → exécuté avant chaque test
    // Prépare les données de test
    @BeforeEach
    void setUp(){
        // Préparer une requête d'inscription
        rClientRequest= new RegisterClientRequest();
        rClientRequest.setNom("Diallo");
        rClientRequest.setPrenom("Abourahmane");
        rClientRequest.setEmail("abdou@test.com");
        rClientRequest.setTelephone("770002378");
        rClientRequest.setAdressePrincipale("Grand-Dakar,dakar");
        rClientRequest.setMotDePasse("passer123");

        // Préparer un client simulé
        client= new Client();
        client.setId(1L);
        client.setNom("Diallo");
        client.setPrenom("Abourahmane");
        client.setEmail("abdou@test.com");
        client.setStatut(StatutActeur.ACTIF);
        client.setRole(RoleUtilisateur.CLIENT);

    }
    // ═══ TEST 1 ═══
    // Inscription client avec email nouveau
    // → doit réussir et retourner un token
    @Test
    void regiisterClientEmailNouveauDoitReussir(){
        // GIVEN — préparer les simulations
        // Email n'existe pas encore
        when(acteurRepository.existsByEmail(anyString())).thenReturn(false);

        // Encoder le mot de passe
        when(passwordEncoder.encode(anyString())).thenReturn("motDePasseEncode");

        // Sauvegarder retourne le client
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // Générer un token JWT
        lenient().when(jwtUtils.genererToken(anyLong(), anyString())).thenReturn("token-jwt-test");
        // WHEN — appeler la méthode à tester
        AuthResponse response = authService.registerClient(rClientRequest);

        // THEN — vérifier le résultat
        assertNotNull(response);
        assertEquals("CLIENT",response.getRole());
//        assertEquals("Client",response.getEmail());
//        assertEquals("abdou@test.com",response.getEmail());
//        assertEquals("Diallo",response.getNom());

        // Vérifier que save() a été appelé 1 fois
        verify(clientRepository, times(1)).save(any(Client.class));


    }

    // ═══ TEST 2 ═══
    // Inscription avec email déjà utilisé
    // → doit lancer une exception
    @Test
    void registerClientEmailDejaUtiliseDoitEchouer(){
        // GIVEN — email existe déjà
        when(acteurRepository.existsByEmail(anyString())).thenReturn(true);
        // WHEN + THEN — vérifier l'exception
        RuntimeException exception= assertThrows(RuntimeException.class,
                ()-> authService.registerClient(rClientRequest));
        assertEquals("Email déjà utilisé",exception.getMessage());

        // Vérifier que save() n'a PAS été appelé
        verify(clientRepository, never()).save(any(Client.class));

    }

    // ═══ TEST 3 ═══
    // Login avec bon mot de passe
    // → doit retourner token + infos
    @Test
    void loginCrendentialsCorrectsDoitReussir(){
        //GIVEN
        AuthRequest request = new AuthRequest();
        request.setEmail("abdou@test.com");
        request.setMotDePasse("passer123");
        client.setMotDePasse("motDePasseEncode");

        when(acteurRepository.findByEmail("abdou@test.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("passer123","motDePasseEncode"))
                .thenReturn(true);
        when(jwtUtils.genererToken(anyLong(),anyString())).thenReturn("token-jwt-test");
        // WHEN
        AuthResponse response = authService.login(request);
        //THEN
        assertNotNull(response);
        assertEquals("CLIENT",response.getRole());
        assertEquals("abdou@test.com",response.getEmail());
    }
    // ═══ TEST 4 ═══
    // Login avec mauvais mot de passe
    // → doit lancer une exception
    @Test
    void loginMauvaiseMotDePasseDoitEchouer(){
        //GIVEN
        AuthRequest request= new AuthRequest();
        request.setEmail("abdou@test.com");
        request.setMotDePasse("MauvaisMotDePasse");

        client.setMotDePasse("motDePasseEncode");

        when(acteurRepository.findByEmail("abdou@test.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("MauvaisMotDePasse", "motDePasseEncode"))
                .thenReturn(false);

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class,()-> authService.login(request));
        assertEquals("Mot de passe incorrect", exception.getMessage());
    }

    // ═══ TEST 5 ═══
    // Login avec email inexistant
    // → doit lancer une exception
    @Test
    void loginEmailInexistantDoitEchouer(){
        //  GIVEN
        AuthRequest request = new AuthRequest();

        request.setEmail("inexiste@test.com");
        request.setMotDePasse("passer123");

        when(acteurRepository.findByEmail("inexiste@test.com")).thenReturn(Optional.empty());

        //WHEN + THEN
        assertThrows(RuntimeException.class, ()-> authService.login(request));

    }


}
