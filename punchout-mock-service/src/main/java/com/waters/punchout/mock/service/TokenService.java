package com.waters.punchout.mock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenService {
    
    // Store one-time tokens in memory
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    
    public String generateOneTimeToken(String sessionKey, String operation) {
        // Generate a full UUID-based one-time token
        String uuid = UUID.randomUUID().toString();
        String token = "OTT-" + uuid;
        
        TokenInfo tokenInfo = new TokenInfo(
            sessionKey,
            operation,
            System.currentTimeMillis(),
            false
        );
        
        tokens.put(token, tokenInfo);
        
        log.info("Generated one-time token: {} for sessionKey: {}", token, sessionKey);
        
        return token;
    }
    
    public boolean validateAndConsumeToken(String token) {
        TokenInfo tokenInfo = tokens.get(token);
        
        if (tokenInfo == null) {
            log.warn("Token not found: {}", token);
            return false;
        }
        
        if (tokenInfo.used) {
            log.warn("Token already used: {}", token);
            return false;
        }
        
        // Check if token expired (30 minutes)
        long tokenAge = System.currentTimeMillis() - tokenInfo.createdAt;
        if (tokenAge > 30 * 60 * 1000) {
            log.warn("Token expired: {}", token);
            tokens.remove(token);
            return false;
        }
        
        // Mark as used
        tokenInfo.used = true;
        log.info("Token validated and consumed: {}", token);
        
        return true;
    }
    
    public int getActiveTokenCount() {
        return (int) tokens.values().stream().filter(t -> !t.used).count();
    }
    
    public void clearExpiredTokens() {
        long now = System.currentTimeMillis();
        tokens.entrySet().removeIf(entry -> 
            now - entry.getValue().createdAt > 30 * 60 * 1000
        );
    }
    
    private static class TokenInfo {
        final String sessionKey;
        final String operation;
        final long createdAt;
        boolean used;
        
        TokenInfo(String sessionKey, String operation, long createdAt, boolean used) {
            this.sessionKey = sessionKey;
            this.operation = operation;
            this.createdAt = createdAt;
            this.used = used;
        }
    }
}
