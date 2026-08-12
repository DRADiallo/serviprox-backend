package com.serviprox.serviprox_backend;

import com.serviprox.serviprox_backend.dto.PrestataireDTO;
import com.serviprox.serviprox_backend.entity.Prestataire;
import com.serviprox.serviprox_backend.enums.StatutActeur;
import com.serviprox.serviprox_backend.repository.PrestataireRepository;
import com.serviprox.serviprox_backend.service.PrestataireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
public class PrestataireServiceTest {
    @Mock
    private PrestataireRepository prestataireRepository;

    @InjectMocks
    private PrestataireService prestataireService;

    private Prestataire prestataire;

    @BeforeEach
    void setUp(){
        prestataire = new Prestataire();
        prestataire.setId(1L);
        prestataire.setNomCommercial("MAYA PRESSING GRD-DKR");
        prestataire.setZoneCouverture("Grd-dakar,POINT E");
        prestataire.setNoteMoyenne(4.5);
        prestataire.setStatut(StatutActeur.ACTIF);
    }

    // ═══ TEST 1 ═══
    // Liste prestataires actifs
    @Test
    void getAllActifsDoitRetounerActifsOnly(){

        // GIVEN
        when(prestataireRepository.findByStatut(StatutActeur.ACTIF))
                .thenReturn(List.of(prestataire));

        //WHEN
        List<PrestataireDTO> result= prestataireService.getAllActifs();

        //THEN
        assertNotNull(result);
        assertEquals(1,result.size());
        assertEquals("MAYA PRESSING GRD-DKR",result.get(0).getNomCommercial());
    }

    // ═══ TEST 2 ═══
    // Prestataire par ID — trouvé
    @Test
    void getByIdPrestataireExisteDoitRetouner(){

        //GIVEN
        when(prestataireRepository.findById(1L)).thenReturn(Optional.of(prestataire));

        //WHEN
        PrestataireDTO result = prestataireService.getById(1L);

        //THEN
        assertNotNull(result);
        assertEquals("MAYA PRESSING GRD-DKR",result.getNomCommercial());
        assertEquals("Grd-dakar,POINT E",result.getZone());

    }

    // ═══ TEST 3 ═══
    // Prestataire par ID — introuvable
    @Test
    void getByIdPrestataireInexistantDoitEchouer(){

        //GIVEN
        when(prestataireRepository.findById(99L)).thenReturn(Optional.empty());

        //WHEN + THEN
        assertThrows(RuntimeException.class,()->prestataireService.getById(99L));

    }

    // ═══ TEST 4 ═══
    // Prestataires par zone
    @Test
    void getByZoneDoitRetournerParZoneCouverture(){
        //GIVEN
        when(prestataireRepository.findByZoneCouverture("Grd-dakar,POINT E"))
                .thenReturn(List.of(prestataire));

        //WHEN
        List<PrestataireDTO>result= prestataireService.getByZone("Grd-dakar,POINT E");

        //THEN
        assertEquals(1,result.size());
        assertEquals("Grd-dakar,POINT E",result.get(0).getZone());

    }

    // ═══ TEST 5 ═══
    // Désactiver prestataire
    @Test
    void desactiverDoitChangerStatutInactif(){

        //GIVEN

        when(prestataireRepository.findById(1L)).thenReturn(Optional.of(prestataire));
        when(prestataireRepository.save(any())).thenReturn(prestataire);

        // WHEN
        PrestataireDTO result = prestataireService.desactiver(1L,"Fraude detectee");

        // THEN
        assertNotNull(result);
        verify(prestataireRepository,times(1)).save(any(Prestataire.class));
    }
    

}
