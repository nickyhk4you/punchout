package com.waters.punchout.gateway.converter.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.resolve.CustomerConfig;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class BaseConverter implements PunchOutConverterStrategy {
    
    protected final ObjectMapper jsonMapper = new ObjectMapper();
    protected final XmlMapper xmlMapper = new XmlMapper();
    
    @Override
    public final PunchOutRequest convert(JsonNode root, ConversionContext ctx) throws Exception {
        log.debug("Converting cXML for customer: {}, version: {}", customerId(), version());
        
        // Step 1: Extract common fields
        PunchOutRequest request = buildCommon(root, ctx);
        
        // Step 2: Apply configuration-driven mappings
        applyConfigMappings(request, root, ctx);
        
        // Step 3: Customer-specific customization hook
        customize(request, root, ctx);
        
        // Step 4: Validation
        validate(request, ctx);
        
        log.debug("Conversion completed for sessionKey: {}", request.getSessionKey());
        return request;
    }
    
    protected PunchOutRequest buildCommon(JsonNode root, ConversionContext ctx) {
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        
        PunchOutRequest request = new PunchOutRequest();
        
        // Extract BuyerCookie as session key
        String buyerCookie = requestNode.path("BuyerCookie").asText();
        if (buyerCookie == null || buyerCookie.isEmpty()) {
            buyerCookie = generateSessionKey();
        }
        request.setSessionKey(buyerCookie);
        request.setBuyerCookie(buyerCookie);
        request.setOperation(requestNode.path("operation").asText("create"));
        request.setTimestamp(LocalDateTime.now());
        
        // Extract contact email
        JsonNode email = requestNode.path("Contact").path("Email");
        if (!email.isMissingNode()) {
            request.setContactEmail(email.asText());
        }
        
        // Extract cart return URL
        JsonNode browserFormPost = requestNode.path("BrowserFormPost").path("URL");
        if (!browserFormPost.isMissingNode()) {
            request.setCartReturnUrl(browserFormPost.asText());
        }
        
        // Extract identities
        JsonNode headerNode = root.path("Header");
        request.setFromIdentity(extractIdentity(headerNode.path("From")));
        request.setToIdentity(extractIdentity(headerNode.path("To")));
        request.setSenderIdentity(extractIdentity(headerNode.path("Sender")));
        
        // Extract all extrinsics into a map
        request.setExtrinsics(extractExtrinsics(requestNode));
        
        log.debug("Built common request: sessionKey={}, operation={}, extrinsics count={}", 
                request.getSessionKey(), request.getOperation(), 
                request.getExtrinsics() != null ? request.getExtrinsics().size() : 0);
        
        return request;
    }
    
    protected void applyConfigMappings(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        CustomerConfig config = ctx.getCustomerConfig();
        if (config == null) {
            log.debug("No customer config available for mapping");
            return;
        }
        
        log.debug("Applying config mappings for customer: {}", config.getId());
        
        // Check if session key should be regenerated
        if (Boolean.TRUE.equals(config.getForceNewSessionKey())) {
            String newKey = generateSessionKey();
            log.debug("Forcing new session key: {} -> {}", request.getSessionKey(), newKey);
            request.setSessionKey(newKey);
        }
    }
    
    // Hook for customer-specific customization
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        // Default: no customization
        log.debug("No customization for customer: {}", customerId());
    }
    
    // Helper methods
    protected Map<String, String> extractExtrinsics(JsonNode requestNode) {
        Map<String, String> extrinsics = new HashMap<>();
        
        JsonNode extrinsicNode = requestNode.path("Extrinsic");
        if (extrinsicNode.isArray()) {
            extrinsicNode.forEach(ex -> {
                String name = ex.path("name").asText(null);
                String value = ex.asText(null);
                if (name != null && value != null) {
                    extrinsics.put(name, value);
                }
            });
        } else if (!extrinsicNode.isMissingNode()) {
            String name = extrinsicNode.path("name").asText(null);
            String value = extrinsicNode.asText(null);
            if (name != null && value != null) {
                extrinsics.put(name, value);
            }
        }
        
        log.debug("Extracted {} extrinsics", extrinsics.size());
        return extrinsics.isEmpty() ? null : extrinsics;
    }
    
    protected String extractIdentity(JsonNode credentialNode) {
        JsonNode identityNode = credentialNode.path("Credential").path("Identity");
        return identityNode.isMissingNode() ? null : identityNode.asText();
    }
    
    protected String generateSessionKey() {
        return "SESSION_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
