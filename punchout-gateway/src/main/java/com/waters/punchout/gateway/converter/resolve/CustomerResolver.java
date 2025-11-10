package com.waters.punchout.gateway.converter.resolve;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.config.PunchoutConversionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerResolver {
    
    private final PunchoutConversionProperties properties;
    
    public ConversionKey resolve(JsonNode root) {
        JsonNode headerNode = root.path("Header");
        
        String fromDomain = headerNode.path("From").path("Credential").path("domain").asText(null);
        String fromIdentity = headerNode.path("From").path("Credential").path("Identity").asText(null);
        String toIdentity = headerNode.path("To").path("Credential").path("Identity").asText(null);
        String userAgent = headerNode.path("Sender").path("UserAgent").asText(null);
        
        log.debug("Resolving customer from: fromDomain={}, fromIdentity={}, toIdentity={}, userAgent={}", 
                fromDomain, fromIdentity, toIdentity, userAgent);
        
        for (CustomerConfig config : properties.getCustomers()) {
            if (matches(config.getMatch(), fromDomain, fromIdentity, toIdentity, userAgent)) {
                log.info("Matched customer: {} version: {}", config.getId(), config.getVersion());
                return new ConversionKey(config.getId(), config.getVersion(), config);
            }
        }
        
        log.debug("No customer match found, using default converter");
        return new ConversionKey("default", "v1", null);
    }
    
    private boolean matches(MatchCriteria criteria, String fromDomain, String fromIdentity, 
                           String toIdentity, String userAgent) {
        if (criteria == null) {
            return false;
        }
        
        // Check fromDomain
        if (criteria.getFromDomain() != null && 
            !criteria.getFromDomain().equalsIgnoreCase(fromDomain)) {
            return false;
        }
        
        // Check fromIdentity (exact match)
        if (criteria.getFromIdentity() != null && 
            !criteria.getFromIdentity().equals(fromIdentity)) {
            return false;
        }
        
        // Check fromIdentity (pattern match)
        if (criteria.getFromIdentityPattern() != null && fromIdentity != null &&
            !fromIdentity.matches(criteria.getFromIdentityPattern())) {
            return false;
        }
        
        // Check toIdentity (exact match)
        if (criteria.getToIdentity() != null && 
            !criteria.getToIdentity().equals(toIdentity)) {
            return false;
        }
        
        // Check toIdentity (pattern match)
        if (criteria.getToIdentityPattern() != null && toIdentity != null &&
            !toIdentity.matches(criteria.getToIdentityPattern())) {
            return false;
        }
        
        // Check userAgent (pattern match)
        if (criteria.getSenderUserAgentPattern() != null && userAgent != null &&
            !userAgent.matches(criteria.getSenderUserAgentPattern())) {
            return false;
        }
        
        return true;
    }
}
