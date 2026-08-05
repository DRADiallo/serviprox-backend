package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.entity.ConfigurationService;
import com.serviprox.serviprox_backend.service.ConfigurationServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// URL : /api/service-config
// Levier openEHR — admin ajoute nouveaux services
// sans modifier le code !
// Ex: admin ajoute "jardinage"
// → Flutter affiche automatiquement
@RestController
@RequestMapping("/api/service-config")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConfigurationServiceController {

    private final ConfigurationServiceService configurationServiceService;

    // POST /api/service-config
    // Admin crée un nouveau type de service
    // SANS modifier le code Spring Boot !
    @PostMapping
    public ResponseEntity<ConfigurationService>creer(@RequestBody ConfigurationService config ){

        return ResponseEntity.ok(configurationServiceService.creer(config));

    }

    // GET /api/service-config/actifs
    // Flutter — services disponibles
    // Seuls les services actifs sont visibles
    @GetMapping("/actifs")
    public ResponseEntity<List<ConfigurationService>> getAllActifs(){

        return ResponseEntity.ok(configurationServiceService.getAllActifs());
    }

    // GET /api/service-config
    // Admin React — tous les services
    // incluant les désactivés
    @GetMapping
    public ResponseEntity<List<ConfigurationService>> getAllAdmin(){

        return ResponseEntity.ok(configurationServiceService.getAll());
    }

    // GET /api/service-config/type/{type}
    // Ex: GET /api/service-config/type/pressing
    @GetMapping("/type/{typeService}")
    public ResponseEntity<ConfigurationService> getByType(@PathVariable String typeService){

        return ResponseEntity.ok(configurationServiceService.getByType(typeService));
    }

    // GET /api/service-config/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ConfigurationService> getById(@PathVariable Long id){

        return ResponseEntity.ok(configurationServiceService.getById(id));
    }

    // PUT /api/service-config/{id}
    // Admin modifie un type de service
    // Ex: ajouter une étape au workflow jardinage
    @PutMapping("/{id}/modifier")
    public ResponseEntity<ConfigurationService>modifier(@PathVariable Long id,@RequestBody ConfigurationService config){

        return ResponseEntity.ok(configurationServiceService.modifier(id,config));
    }

    // PUT /api/service-config/{id}/activer
    // Rendre service visible sur Flutter
    @PutMapping("/{id}/activer")
    public ResponseEntity<ConfigurationService>activer(@PathVariable Long id){

        return ResponseEntity.ok(configurationServiceService.activer(id));
    }

    // PUT /api/service-config/{id}/desactiver
    // Cacher service sans supprimer
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<ConfigurationService>desactiver(@PathVariable Long id){

        return ResponseEntity.ok(configurationServiceService.desactiver(id));
    }

    // POST /api/service-config/initialiser
    // Initialiser les 3 services par défaut
    // À appeler une seule fois au démarrage
    @PostMapping("/initialiser")
    public ResponseEntity<Void> initialiser(){

        configurationServiceService.initialiserParDefaut();
        return ResponseEntity.ok().build();
    }








}
