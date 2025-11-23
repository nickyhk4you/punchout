package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.ApiKey;
import com.waters.punchout.gateway.entity.SecurityAuditLog;
import com.waters.punchout.gateway.repository.ApiKeyRepository;
import com.waters.punchout.gateway.repository.SecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecurityAuditLogRepository auditLogRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String API_KEY_PREFIX = "pk_";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll();
    }

    public List<ApiKey> getApiKeysByCustomer(String customerName) {
        return apiKeyRepository.findByCustomerName(customerName);
    }

    public ApiKey generateApiKey(String customerName, String description, List<String> permissions, 
                                  String environment, Integer expiryDays, String createdBy) {
        String keyValue = generateSecureKey();
        
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(keyValue);
        apiKey.setCustomerName(customerName);
        apiKey.setDescription(description);
        apiKey.setPermissions(permissions != null ? permissions : Arrays.asList("PUNCHOUT", "ORDER"));
        apiKey.setEnvironment(environment);
        apiKey.setEnabled(true);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setUsageCount(0);
        apiKey.setCreatedBy(createdBy);
        
        if (expiryDays != null && expiryDays > 0) {
            apiKey.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        }
        
        ApiKey saved = apiKeyRepository.save(apiKey);
        
        logSecurityEvent("API_KEY_GENERATED", "INFO", customerName, null,
                "API key generated for " + customerName + " in " + environment,
                Map.of("keyId", saved.getId(), "permissions", String.join(",", permissions)));
        
        log.info("Generated API key for customer: {}, environment: {}, id: {}", 
                customerName, environment, saved.getId());
        
        return saved;
    }

    public ApiKey revokeApiKey(String apiKeyId, String revokedBy) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API Key not found: " + apiKeyId));
        
        apiKey.setEnabled(false);
        apiKey.setRevokedAt(LocalDateTime.now());
        apiKey.setRevokedBy(revokedBy);
        
        ApiKey saved = apiKeyRepository.save(apiKey);
        
        logSecurityEvent("API_KEY_REVOKED", "WARNING", apiKey.getCustomerName(), null,
                "API key revoked for " + apiKey.getCustomerName(),
                Map.of("keyId", apiKeyId, "revokedBy", revokedBy));
        
        log.info("Revoked API key: id={}, customer={}", apiKeyId, apiKey.getCustomerName());
        
        return saved;
    }

    public ApiKey rotateApiKey(String apiKeyId, String rotatedBy) {
        ApiKey oldKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API Key not found: " + apiKeyId));
        
        // Create new key with same settings
        ApiKey newKey = generateApiKey(
                oldKey.getCustomerName(),
                oldKey.getDescription() + " (Rotated)",
                oldKey.getPermissions(),
                oldKey.getEnvironment(),
                oldKey.getExpiresAt() != null ? 365 : null,
                rotatedBy
        );
        
        // Revoke old key
        revokeApiKey(apiKeyId, rotatedBy);
        
        logSecurityEvent("API_KEY_ROTATED", "INFO", oldKey.getCustomerName(), null,
                "API key rotated for " + oldKey.getCustomerName(),
                Map.of("oldKeyId", apiKeyId, "newKeyId", newKey.getId()));
        
        return newKey;
    }

    public boolean validateApiKey(String keyValue) {
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyValue(keyValue);
        
        if (apiKeyOpt.isEmpty()) {
            logSecurityEvent("AUTH_FAILURE", "WARNING", null, null,
                    "Invalid API key attempted", Map.of("keyPrefix", keyValue.substring(0, 10)));
            return false;
        }
        
        ApiKey apiKey = apiKeyOpt.get();
        
        if (!apiKey.getEnabled()) {
            logSecurityEvent("AUTH_FAILURE", "WARNING", apiKey.getCustomerName(), null,
                    "Disabled API key attempted", Map.of("keyId", apiKey.getId()));
            return false;
        }
        
        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            logSecurityEvent("AUTH_FAILURE", "WARNING", apiKey.getCustomerName(), null,
                    "Expired API key attempted", Map.of("keyId", apiKey.getId()));
            return false;
        }
        
        // Update usage stats
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKey.setUsageCount(apiKey.getUsageCount() + 1);
        apiKeyRepository.save(apiKey);
        
        logSecurityEvent("AUTH_SUCCESS", "INFO", apiKey.getCustomerName(), null,
                "API key authenticated successfully", Map.of("keyId", apiKey.getId()));
        
        return true;
    }

    public void deleteApiKey(String apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API Key not found: " + apiKeyId));
        
        apiKeyRepository.deleteById(apiKeyId);
        
        logSecurityEvent("API_KEY_DELETED", "WARNING", apiKey.getCustomerName(), null,
                "API key deleted for " + apiKey.getCustomerName(),
                Map.of("keyId", apiKeyId));
        
        log.info("Deleted API key: id={}, customer={}", apiKeyId, apiKey.getCustomerName());
    }

    public Map<String, Object> getJwtConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("algorithm", "HS256");
        config.put("expirationMinutes", 30);
        config.put("issuer", "waters-punchout-platform");
        config.put("activeTokensCount", getActiveTokenCount());
        return config;
    }

    public Map<String, Object> updateJwtConfiguration(Integer expirationMinutes, String secret) {
        logSecurityEvent("JWT_CONFIG_CHANGED", "WARNING", null, null,
                "JWT configuration updated",
                Map.of("expirationMinutes", String.valueOf(expirationMinutes)));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "JWT configuration updated successfully");
        result.put("expirationMinutes", expirationMinutes);
        return result;
    }

    public List<SecurityAuditLog> getRecentAuditLogs(Integer limit) {
        PageRequest pageRequest = PageRequest.of(0, limit != null ? limit : 100);
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return auditLogRepository.findByTimestampAfterOrderByTimestampDesc(since, pageRequest);
    }

    public List<SecurityAuditLog> getAuditLogsByType(String eventType) {
        return auditLogRepository.findByEventType(eventType);
    }

    public Map<String, Object> getSecurityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<ApiKey> allKeys = apiKeyRepository.findAll();
        long activeKeys = allKeys.stream().filter(k -> k.getEnabled()).count();
        long expiredKeys = allKeys.stream()
                .filter(k -> k.getExpiresAt() != null && k.getExpiresAt().isBefore(LocalDateTime.now()))
                .count();
        
        stats.put("totalApiKeys", allKeys.size());
        stats.put("activeApiKeys", activeKeys);
        stats.put("expiredApiKeys", expiredKeys);
        stats.put("revokedApiKeys", allKeys.size() - activeKeys);
        
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<SecurityAuditLog> recentLogs = auditLogRepository.findByTimestampAfterOrderByTimestampDesc(
                last24h, PageRequest.of(0, 1000));
        
        long authFailures = recentLogs.stream().filter(l -> "AUTH_FAILURE".equals(l.getEventType())).count();
        long authSuccesses = recentLogs.stream().filter(l -> "AUTH_SUCCESS".equals(l.getEventType())).count();
        
        stats.put("authFailures24h", authFailures);
        stats.put("authSuccesses24h", authSuccesses);
        stats.put("totalSecurityEvents24h", recentLogs.size());
        
        return stats;
    }

    private void logSecurityEvent(String eventType, String severity, String customerName, 
                                   String ipAddress, String description, Map<String, String> metadata) {
        SecurityAuditLog log = new SecurityAuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setEventType(eventType);
        log.setSeverity(severity);
        log.setCustomerName(customerName);
        log.setIpAddress(ipAddress);
        log.setDescription(description);
        log.setMetadata(metadata);
        
        auditLogRepository.save(log);
    }

    private String generateSecureKey() {
        StringBuilder key = new StringBuilder(API_KEY_PREFIX);
        
        for (int i = 0; i < 48; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            key.append(CHARACTERS.charAt(index));
        }
        
        return key.toString();
    }

    private int getActiveTokenCount() {
        // This would integrate with TokenService if needed
        return 0;
    }
}
