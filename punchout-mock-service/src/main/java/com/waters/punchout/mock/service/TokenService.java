package com.waters.punchout.mock.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenService {
    
    @Value("${jwt.secret:punchout-mock-service-secret-key-for-jwt-token-generation-minimum-32-chars}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration; // 30 minutes in milliseconds
    
    // Store used token JTIs to prevent reuse
    private final Set<String> usedTokens = ConcurrentHashMap.newKeySet();
    
    public String generateOneTimeToken(String sessionKey, String operation) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .setId(jti)
                .setSubject(sessionKey)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("operation", operation)
                .claim("type", "one-time-token")
                .claim("sessionKey", sessionKey)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        log.info("Generated JWT one-time token for sessionKey: {}, jti: {}, expires: {}", 
                sessionKey, jti, expiryDate);
        
        return token;
    }
    
    public boolean validateAndConsumeToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String jti = claims.getId();
            String sessionKey = claims.getSubject();
            String type = claims.get("type", String.class);
            
            // Verify it's a one-time token
            if (!"one-time-token".equals(type)) {
                log.warn("Invalid token type: {}", type);
                return false;
            }
            
            // Check if token already used
            if (usedTokens.contains(jti)) {
                log.warn("Token already used: jti={}, sessionKey={}", jti, sessionKey);
                return false;
            }
            
            // Check expiration (JWT library already validates this)
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("Token expired: jti={}, expired at: {}", jti, expiration);
                return false;
            }
            
            // Mark as used
            usedTokens.add(jti);
            log.info("JWT token validated and consumed: jti={}, sessionKey={}", jti, sessionKey);
            
            return true;
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public int getActiveTokenCount() {
        return usedTokens.size();
    }
    
    public void clearExpiredTokens() {
        // JWT tokens are self-expiring, just clear the used tokens set periodically
        if (usedTokens.size() > 10000) {
            usedTokens.clear();
            log.info("Cleared used tokens cache");
        }
    }
    
    public Claims getTokenClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }
}
