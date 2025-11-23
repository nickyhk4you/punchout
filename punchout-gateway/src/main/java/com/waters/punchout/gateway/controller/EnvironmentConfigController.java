package com.waters.punchout.gateway.controller;

import com.waters.punchout.gateway.entity.EnvironmentConfig;
import com.waters.punchout.gateway.service.EnvironmentConfigService;
import com.waters.punchout.gateway.util.EnvironmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/environment-config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EnvironmentConfigController {
    
    private final EnvironmentConfigService environmentConfigService;
    
    @GetMapping
    public ResponseEntity<List<EnvironmentConfig>> getAllConfigs() {
        log.info("GET /api/environment-config - Fetching all configurations");
        List<EnvironmentConfig> configs = environmentConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{environment}")
    public ResponseEntity<EnvironmentConfig> getConfig(@PathVariable String environment) {
        String normalizedEnv = EnvironmentUtil.normalize(environment);
        log.info("GET /api/environment-config/{} (normalized: {}) - Fetching configuration", environment, normalizedEnv);
        EnvironmentConfig config = environmentConfigService.getConfig(normalizedEnv);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/current")
    public ResponseEntity<EnvironmentConfig> getCurrentConfig() {
        log.info("GET /api/environment-config/current - Fetching current configuration");
        EnvironmentConfig config = environmentConfigService.getCurrentConfig();
        return ResponseEntity.ok(config);
    }
    
    @PostMapping
    public ResponseEntity<EnvironmentConfig> createConfig(@RequestBody EnvironmentConfig config) {
        log.info("POST /api/environment-config - Creating configuration for: {}", config.getEnvironment());
        
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        
        EnvironmentConfig saved = environmentConfigService.saveConfig(config);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/{environment}")
    public ResponseEntity<EnvironmentConfig> updateConfig(
            @PathVariable String environment,
            @RequestBody EnvironmentConfig config) {
        String normalizedEnv = EnvironmentUtil.normalize(environment);
        log.info("PUT /api/environment-config/{} (normalized: {}) - Updating configuration", environment, normalizedEnv);
        
        config.setEnvironment(normalizedEnv);
        config.setUpdatedAt(LocalDateTime.now());
        
        EnvironmentConfig saved = environmentConfigService.saveConfig(config);
        return ResponseEntity.ok(saved);
    }
    
    @DeleteMapping("/{environment}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String environment) {
        String normalizedEnv = EnvironmentUtil.normalize(environment);
        log.info("DELETE /api/environment-config/{} (normalized: {}) - Deleting configuration", environment, normalizedEnv);
        environmentConfigService.deleteConfig(normalizedEnv);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/cache/clear/{environment}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String environment) {
        String normalizedEnv = EnvironmentUtil.normalize(environment);
        log.info("POST /api/environment-config/cache/clear/{} (normalized: {}) - Clearing cache", environment, normalizedEnv);
        environmentConfigService.clearCache(normalizedEnv);
        return ResponseEntity.ok(Map.of(
            "message", "Cache cleared for environment: " + normalizedEnv,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    @PostMapping("/cache/clear-all")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        log.info("POST /api/environment-config/cache/clear-all - Clearing all caches");
        environmentConfigService.clearAllCaches();
        return ResponseEntity.ok(Map.of(
            "message", "All caches cleared",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    @GetMapping("/urls/{environment}")
    public ResponseEntity<Map<String, String>> getUrls(@PathVariable String environment) {
        String normalizedEnv = EnvironmentUtil.normalize(environment);
        log.info("GET /api/environment-config/urls/{} (normalized: {}) - Fetching URLs", environment, normalizedEnv);
        
        return ResponseEntity.ok(Map.of(
            "environment", normalizedEnv,
            "authServiceUrl", environmentConfigService.getAuthServiceUrl(normalizedEnv),
            "muleServiceUrl", environmentConfigService.getMuleServiceUrl(normalizedEnv),
            "catalogBaseUrl", environmentConfigService.getCatalogBaseUrl(normalizedEnv)
        ));
    }
}
