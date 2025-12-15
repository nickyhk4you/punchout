package com.waters.punchout.mongo.controller;

import com.waters.punchout.mongo.entity.EnvironmentConfigDocument;
import com.waters.punchout.mongo.service.EnvironmentConfigMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environment-configs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EnvironmentConfigMongoController {
    
    private final EnvironmentConfigMongoService service;
    
    @GetMapping
    public ResponseEntity<List<EnvironmentConfigDocument>> getAll() {
        log.info("GET /api/environment-configs - Fetching all configurations");
        List<EnvironmentConfigDocument> configs = service.findAll();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/enabled")
    public ResponseEntity<List<EnvironmentConfigDocument>> getAllEnabled() {
        log.info("GET /api/environment-configs/enabled - Fetching all enabled configurations");
        List<EnvironmentConfigDocument> configs = service.findAllEnabled();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentConfigDocument> getById(@PathVariable String id) {
        log.info("GET /api/environment-configs/{} - Fetching configuration", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/environment/{environment}")
    public ResponseEntity<EnvironmentConfigDocument> getByEnvironment(@PathVariable String environment) {
        log.info("GET /api/environment-configs/environment/{} - Fetching configuration", environment);
        return service.findByEnvironment(environment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<EnvironmentConfigDocument> create(@RequestBody EnvironmentConfigDocument config) {
        log.info("POST /api/environment-configs - Creating configuration for: {}", config.getEnvironment());
        
        if (service.existsByEnvironment(config.getEnvironment())) {
            return ResponseEntity.badRequest().build();
        }
        
        EnvironmentConfigDocument saved = service.save(config);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentConfigDocument> update(
            @PathVariable String id,
            @RequestBody EnvironmentConfigDocument config) {
        log.info("PUT /api/environment-configs/{} - Updating configuration", id);
        
        try {
            EnvironmentConfigDocument updated = service.update(id, config);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/environment-configs/{} - Deleting configuration", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
