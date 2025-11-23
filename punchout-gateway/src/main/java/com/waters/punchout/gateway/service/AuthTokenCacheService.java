package com.waters.punchout.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Caches auth tokens to reduce calls to the auth service.
 * Tokens are cached by environment and credentials combination.
 */
@Service
@Slf4j
public class AuthTokenCacheService {

    /**
     * Get cached token or return null if not cached.
     * Cache key is based on environment + email combination.
     * TTL is 30 minutes (configured in application.yml).
     * 
     * @param environment The environment (dev, stage, prod)
     * @param email The auth email
     * @return Cached token or null
     */
    @Cacheable(value = "authTokens", key = "#environment + ':' + #email", unless = "#result == null")
    public String getCachedToken(String environment, String email) {
        // Cache miss - return null
        log.debug("Token cache miss for environment={}, email={}", environment, email);
        return null;
    }

    /**
     * Cache a token for the given environment and email.
     * 
     * @param environment The environment
     * @param email The auth email
     * @param token The auth token to cache
     */
    public void cacheToken(String environment, String email, String token) {
        // The @Cacheable annotation on getCachedToken will handle caching
        // We need to manually put the value in the cache
        log.info("Caching auth token for environment={}, email={}", environment, email);
        // Spring Cache doesn't provide a direct @CachePut without invoking the method
        // We'll use a workaround via a cacheable method that returns the token
        putTokenInCache(environment, email, token);
    }

    @Cacheable(value = "authTokens", key = "#environment + ':' + #email")
    protected String putTokenInCache(String environment, String email, String token) {
        return token;
    }

    /**
     * Evict cached token for a specific environment/email combination.
     * Use when token is known to be invalid.
     */
    @CacheEvict(value = "authTokens", key = "#environment + ':' + #email")
    public void evictToken(String environment, String email) {
        log.info("Evicting cached token for environment={}, email={}", environment, email);
    }

    /**
     * Evict all cached tokens.
     */
    @CacheEvict(value = "authTokens", allEntries = true)
    public void evictAllTokens() {
        log.info("Evicting all cached auth tokens");
    }
}
