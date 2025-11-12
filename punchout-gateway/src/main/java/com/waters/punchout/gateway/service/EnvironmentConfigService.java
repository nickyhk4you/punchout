package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.EnvironmentConfig;
import com.waters.punchout.gateway.repository.EnvironmentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentConfigService {
    
    private final EnvironmentConfigRepository repository;
    
    @Value("${app.environment:dev}")
    private String currentEnvironment;
    
    @Value("${thirdparty.auth.url:#{null}}")
    private String fallbackAuthUrl;
    
    @Value("${thirdparty.mule.url:#{null}}")
    private String fallbackMuleUrl;
    
    @PostConstruct
    public void init() {
        log.info("Environment Config Service initialized. Current environment: {}", currentEnvironment);
        log.info("Fallback Auth URL: {}", fallbackAuthUrl);
        log.info("Fallback Mule URL: {}", fallbackMuleUrl);
    }
    
    /**
     * Get configuration for current environment (caching disabled)
     */
    // @Cacheable(value = "environmentConfig", key = "#environment") - DISABLED to avoid cache issues
    public EnvironmentConfig getConfig(String environment) {
        log.info("Loading configuration for environment: {}", environment);
        
        return repository.findByEnvironmentAndEnabledTrue(environment)
                .orElseGet(() -> {
                    log.warn("No enabled configuration found for environment: {}. Using fallback values.", environment);
                    return createFallbackConfig(environment);
                });
    }
    
    /**
     * Get configuration for current environment
     */
    public EnvironmentConfig getCurrentConfig() {
        return getConfig(currentEnvironment);
    }
    
    /**
     * Get auth service URL for specific environment
     */
    public String getAuthServiceUrl(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getAuthServiceUrl();
    }
    
    /**
     * Get auth service URL for current environment
     */
    public String getAuthServiceUrl() {
        return getAuthServiceUrl(currentEnvironment);
    }
    
    /**
     * Get mule service URL for specific environment
     */
    public String getMuleServiceUrl(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getMuleServiceUrl();
    }
    
    /**
     * Get mule service URL for current environment
     */
    public String getMuleServiceUrl() {
        return getMuleServiceUrl(currentEnvironment);
    }
    
    /**
     * Get catalog base URL for specific environment
     */
    public String getCatalogBaseUrl(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getCatalogBaseUrl();
    }
    
    /**
     * Get catalog base URL for current environment
     */
    public String getCatalogBaseUrl() {
        return getCatalogBaseUrl(currentEnvironment);
    }
    
    /**
     * Get all configurations
     */
    public List<EnvironmentConfig> getAllConfigs() {
        return repository.findAll();
    }
    
    /**
     * Save or update configuration
     */
    // @CacheEvict(value = "environmentConfig", key = "#config.environment") - Cache disabled
    public EnvironmentConfig saveConfig(EnvironmentConfig config) {
        log.info("Saving configuration for environment: {}", config.getEnvironment());
        return repository.save(config);
    }
    
    /**
     * Delete configuration
     */
    // @CacheEvict(value = "environmentConfig", key = "#environment") - Cache disabled
    public void deleteConfig(String environment) {
        log.info("Deleting configuration for environment: {}", environment);
        repository.findByEnvironment(environment)
                .ifPresent(repository::delete);
    }
    
    /**
     * Clear cache for specific environment (no-op when cache disabled)
     */
    // @CacheEvict(value = "environmentConfig", key = "#environment") - Cache disabled
    public void clearCache(String environment) {
        log.info("Cache clearing skipped - caching is disabled for environment: {}", environment);
    }
    
    /**
     * Clear all caches (no-op when cache disabled)
     */
    // @CacheEvict(value = "environmentConfig", allEntries = true) - Cache disabled
    public void clearAllCaches() {
        log.info("Cache clearing skipped - caching is disabled");
    }
    
    /**
     * Create fallback configuration when no database config exists
     */
    private EnvironmentConfig createFallbackConfig(String environment) {
        EnvironmentConfig fallback = new EnvironmentConfig();
        fallback.setEnvironment(environment);
        fallback.setAuthServiceUrl(fallbackAuthUrl != null ? fallbackAuthUrl : "http://localhost:8081/api/auth/token");
        fallback.setMuleServiceUrl(fallbackMuleUrl != null ? fallbackMuleUrl : "http://localhost:8082/api/catalog");
        fallback.setCatalogBaseUrl("http://localhost:3000");
        fallback.setDescription("Fallback configuration from application.properties");
        fallback.setEnabled(true);
        return fallback;
    }
}
