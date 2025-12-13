package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.EnvironmentConfig;
import com.waters.punchout.gateway.repository.EnvironmentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired(required = false)
    @org.springframework.beans.factory.annotation.Qualifier("jasyptStringEncryptor")
    private StringEncryptor stringEncryptor;
    
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
        log.info("Jasypt StringEncryptor available: {}", stringEncryptor != null);
        if (stringEncryptor != null) {
            log.info("Jasypt StringEncryptor class: {}", stringEncryptor.getClass().getName());
        } else {
            log.warn("Jasypt StringEncryptor is NULL - encrypted passwords will not work!");
        }
    }
    
    /**
     * Get configuration for current environment
     */
    @Cacheable(value = "environmentConfig", key = "#environment")
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
     * Get auth email for specific environment
     */
    public String getAuthEmail(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getAuthEmail();
    }
    
    /**
     * Get auth email for current environment
     */
    public String getAuthEmail() {
        return getAuthEmail(currentEnvironment);
    }
    
    /**
     * Get auth password for specific environment (decrypts if encrypted)
     */
    public String getAuthPassword(String environment) {
        EnvironmentConfig config = getConfig(environment);
        String encryptedPassword = config.getAuthPassword();
        
        if (encryptedPassword == null) {
            return null;
        }
        
        // Check if password is encrypted (format: ENC(...))
        if (encryptedPassword.startsWith("ENC(") && encryptedPassword.endsWith(")")) {
            if (stringEncryptor != null) {
                try {
                    String encrypted = encryptedPassword.substring(4, encryptedPassword.length() - 1);
                    String decrypted = stringEncryptor.decrypt(encrypted);
                    log.info("Successfully decrypted password for environment: {}", environment);
                    return decrypted;
                } catch (Exception e) {
                    log.error("Failed to decrypt password for environment: {}", environment);
                    throw new RuntimeException("Failed to decrypt password for environment: " + environment, e);
                }
            } else {
                log.warn("Password is encrypted but Jasypt encryptor not available for environment: {}", environment);
                return encryptedPassword;
            }
        }
        
        // Password is plain text (not recommended, but supported for backward compatibility)
        log.warn("Using plain text password for environment: {} (consider encrypting)", environment);
        return encryptedPassword;
    }
    
    /**
     * Get auth password for current environment
     */
    public String getAuthPassword() {
        return getAuthPassword(currentEnvironment);
    }
    
    /**
     * Get timeout for specific environment
     */
    public Integer getTimeout(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getTimeout() != null ? config.getTimeout() : 30000; // Default 30 seconds
    }
    
    /**
     * Get retry attempts for specific environment
     */
    public Integer getRetryAttempts(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getRetryAttempts() != null ? config.getRetryAttempts() : 3; // Default 3 retries
    }
    
    /**
     * Get health check URL for specific environment
     */
    public String getHealthCheckUrl(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getHealthCheckUrl();
    }
    
    /**
     * Check if sensitive data should be masked for specific environment
     */
    public boolean shouldMaskSensitiveData(String environment) {
        EnvironmentConfig config = getConfig(environment);
        return config.getMaskSensitiveData() != null ? config.getMaskSensitiveData() : true;
    }
    
    /**
     * Check if sensitive data should be masked for current environment
     */
    public boolean shouldMaskSensitiveData() {
        return shouldMaskSensitiveData(currentEnvironment);
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
    @CacheEvict(value = "environmentConfig", key = "#config.environment")
    public EnvironmentConfig saveConfig(EnvironmentConfig config) {
        log.info("Saving configuration for environment: {}", config.getEnvironment());
        return repository.save(config);
    }
    
    /**
     * Delete configuration
     */
    @CacheEvict(value = "environmentConfig", key = "#environment")
    public void deleteConfig(String environment) {
        log.info("Deleting configuration for environment: {}", environment);
        repository.findByEnvironment(environment)
                .ifPresent(repository::delete);
    }
    
    /**
     * Clear cache for specific environment
     */
    @CacheEvict(value = "environmentConfig", key = "#environment")
    public void clearCache(String environment) {
        log.info("Clearing cache for environment: {}", environment);
    }
    
    /**
     * Clear all caches
     */
    @CacheEvict(value = "environmentConfig", allEntries = true)
    public void clearAllCaches() {
        log.info("Clearing all environment config caches");
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
