package com.waters.punchout.gateway.controller;

import com.waters.punchout.gateway.entity.ApiKey;
import com.waters.punchout.gateway.entity.SecurityAuditLog;
import com.waters.punchout.gateway.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKey>> getAllApiKeys() {
        log.info("GET /api/security/api-keys - Fetching all API keys");
        return ResponseEntity.ok(securityService.getAllApiKeys());
    }

    @GetMapping("/api-keys/customer/{customerName}")
    public ResponseEntity<List<ApiKey>> getApiKeysByCustomer(@PathVariable String customerName) {
        log.info("GET /api/security/api-keys/customer/{} - Fetching API keys", customerName);
        return ResponseEntity.ok(securityService.getApiKeysByCustomer(customerName));
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKey> generateApiKey(@RequestBody Map<String, Object> request) {
        log.info("POST /api/security/api-keys - Generating new API key");
        
        String customerName = (String) request.get("customerName");
        String description = (String) request.get("description");
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) request.get("permissions");
        String environment = (String) request.get("environment");
        Integer expiryDays = request.get("expiryDays") != null ? (Integer) request.get("expiryDays") : null;
        String createdBy = (String) request.getOrDefault("createdBy", "admin");
        
        ApiKey apiKey = securityService.generateApiKey(
                customerName, description, permissions, environment, expiryDays, createdBy);
        
        return ResponseEntity.ok(apiKey);
    }

    @PostMapping("/api-keys/{id}/revoke")
    public ResponseEntity<ApiKey> revokeApiKey(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {
        log.info("POST /api/security/api-keys/{}/revoke - Revoking API key", id);
        
        String revokedBy = request != null ? request.get("revokedBy") : "admin";
        ApiKey apiKey = securityService.revokeApiKey(id, revokedBy);
        
        return ResponseEntity.ok(apiKey);
    }

    @PostMapping("/api-keys/{id}/rotate")
    public ResponseEntity<ApiKey> rotateApiKey(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {
        log.info("POST /api/security/api-keys/{}/rotate - Rotating API key", id);
        
        String rotatedBy = request != null ? request.get("rotatedBy") : "admin";
        ApiKey newKey = securityService.rotateApiKey(id, rotatedBy);
        
        return ResponseEntity.ok(newKey);
    }

    @DeleteMapping("/api-keys/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable String id) {
        log.info("DELETE /api/security/api-keys/{} - Deleting API key", id);
        securityService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jwt/config")
    public ResponseEntity<Map<String, Object>> getJwtConfiguration() {
        log.info("GET /api/security/jwt/config - Fetching JWT configuration");
        return ResponseEntity.ok(securityService.getJwtConfiguration());
    }

    @PutMapping("/jwt/config")
    public ResponseEntity<Map<String, Object>> updateJwtConfiguration(@RequestBody Map<String, Object> config) {
        log.info("PUT /api/security/jwt/config - Updating JWT configuration");
        
        Integer expirationMinutes = config.get("expirationMinutes") != null 
                ? (Integer) config.get("expirationMinutes") : 30;
        String secret = (String) config.get("secret");
        
        return ResponseEntity.ok(securityService.updateJwtConfiguration(expirationMinutes, secret));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<SecurityAuditLog>> getAuditLogs(
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        log.info("GET /api/security/audit-logs - Fetching audit logs, limit: {}", limit);
        return ResponseEntity.ok(securityService.getRecentAuditLogs(limit));
    }

    @GetMapping("/audit-logs/type/{eventType}")
    public ResponseEntity<List<SecurityAuditLog>> getAuditLogsByType(@PathVariable String eventType) {
        log.info("GET /api/security/audit-logs/type/{} - Fetching audit logs by type", eventType);
        return ResponseEntity.ok(securityService.getAuditLogsByType(eventType));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSecurityStatistics() {
        log.info("GET /api/security/statistics - Fetching security statistics");
        return ResponseEntity.ok(securityService.getSecurityStatistics());
    }
}
