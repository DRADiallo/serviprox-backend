package com.serviprox.serviprox_backend.controller;


import com.serviprox.serviprox_backend.entity.ConfigurationService;
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

    private final ConfigurationService configurationService;

    // POST /api/service-config
    // Admin crée un nouveau type de service
    // SANS modifier le code Spring Boot !
    @PostMapping
    public ResponseEntity<ConfigurationService>creer(@RequestBody ConfigurationService config ){

        return ResponseEntity.ok(configurationService.creer(config));

    }

    // GET /api/service-config/actifs
    // Flutter — services disponibles
    // Seuls les services actifs sont visibles
    @GetMapping("/actifs")
    public ResponseEntity<List<ConfigurationService>> getAllActifs(){

        return ResponseEntity.ok(configurationService.getActif());
    }

    // GET /api/service-config
    // Admin React — tous les services
    // incluant les désactivés
    @GetMapping
    public ResponseEntity<List<ConfigurationService>> getAllAdmin(){

        return ResponseEntity.ok(configurationService.getAllAdmin());
    }

    // GET /api/service-config/type/{type}
    // Ex: GET /api/service-config/type/pressing
    @GetMapping("/type/{typeService}")
    public ResponseEntity<ConfigurationService> getByType(@PathVariable String typeService){

        return ResponseEntity.ok(configurationService.getByType(typeService);
    }

    // GET /api/service-config/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ConfigurationService> getById(@PathVariable Long id){

        return ResponseEntity.ok(configurationService.getByType(id));
    }

    // PUT /api/service-config/{id}
    // Admin modifie un type de service
    // Ex: ajouter une étape au workflow jardinage
    @PutMapping("/{id}")
    public ResponseEntity<ConfigurationService>modifier(@PathVariable Long id,@RequestBody ConfigurationService config){

        return ResponseEntity.ok(configurationService.modifier(id,config);
    }

    // PUT /api/service-config/{id}/activer
    // Rendre service visible sur Flutter
    @PutMapping("/{id}")
    public ResponseEntity<ConfigurationService>activer(@PathVariable Long id){

        return ResponseEntity.ok(configurationService.activer(id));
    }

    // PUT /api/service-config/{id}/desactiver
    // Cacher service sans supprimer
    @PutMapping("/{id}")
    public ResponseEntity<ConfigurationService>desactiver(@PathVariable Long id){

        return ResponseEntity.ok(configurationService.desactiver(id));
    }

    // POST /api/service-config/initialiser
    // Initialiser les 3 services par défaut
    // À appeler une seule fois au démarrage
    @PostMapping("/initialiser")
    public ResponseEntity<Void> initialiser(){

        configurationService.initialiserParDefaut();
        return ResponseEntity.ok().build();
    }







}
