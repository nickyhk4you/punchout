package com.waters.punchout.mongo.service;

import com.waters.punchout.mongo.entity.EnvironmentConfigDocument;
import com.waters.punchout.mongo.repository.EnvironmentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentConfigMongoService {
    
    private final EnvironmentConfigRepository repository;
    
    public List<EnvironmentConfigDocument> findAll() {
        log.debug("Finding all environment configs");
        return repository.findAll();
    }
    
    public List<EnvironmentConfigDocument> findAllEnabled() {
        log.debug("Finding all enabled environment configs");
        return repository.findByEnabledTrue();
    }
    
    public Optional<EnvironmentConfigDocument> findByEnvironment(String environment) {
        log.debug("Finding environment config: {}", environment);
        return repository.findByEnvironment(normalizeEnvironment(environment));
    }
    
    public Optional<EnvironmentConfigDocument> findById(String id) {
        log.debug("Finding environment config by id: {}", id);
        return repository.findById(id);
    }
    
    public EnvironmentConfigDocument save(EnvironmentConfigDocument config) {
        log.info("Saving environment config: {}", config.getEnvironment());
        
        config.setEnvironment(normalizeEnvironment(config.getEnvironment()));
        
        if (config.getId() == null) {
            config.setCreatedAt(LocalDateTime.now());
            config.setCreatedBy("system");
        }
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy("system");
        
        return repository.save(config);
    }
    
    public EnvironmentConfigDocument update(String id, EnvironmentConfigDocument config) {
        log.info("Updating environment config: {}", id);
        
        Optional<EnvironmentConfigDocument> existing = repository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Environment config not found: " + id);
        }
        
        EnvironmentConfigDocument doc = existing.get();
        doc.setEnvironment(normalizeEnvironment(config.getEnvironment()));
        doc.setAuthServiceUrl(config.getAuthServiceUrl());
        doc.setAuthEmail(config.getAuthEmail());
        if (config.getAuthPassword() != null && !config.getAuthPassword().isEmpty()) {
            doc.setAuthPassword(config.getAuthPassword());
        }
        doc.setMuleServiceUrl(config.getMuleServiceUrl());
        doc.setCatalogBaseUrl(config.getCatalogBaseUrl());
        doc.setTimeout(config.getTimeout());
        doc.setRetryAttempts(config.getRetryAttempts());
        doc.setHealthCheckUrl(config.getHealthCheckUrl());
        doc.setDescription(config.getDescription());
        doc.setEnabled(config.getEnabled());
        doc.setUpdatedAt(LocalDateTime.now());
        doc.setUpdatedBy("system");
        
        return repository.save(doc);
    }
    
    public void delete(String id) {
        log.info("Deleting environment config: {}", id);
        repository.deleteById(id);
    }
    
    public void deleteByEnvironment(String environment) {
        log.info("Deleting environment config by environment: {}", environment);
        repository.deleteByEnvironment(normalizeEnvironment(environment));
    }
    
    public boolean existsByEnvironment(String environment) {
        return repository.existsByEnvironment(normalizeEnvironment(environment));
    }
    
    private String normalizeEnvironment(String environment) {
        if (environment == null) {
            return null;
        }
        return environment.toLowerCase().trim();
    }
}
