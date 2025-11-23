package com.waters.punchout.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.gateway.client.AuthServiceClient;
import com.waters.punchout.gateway.client.MuleServiceClient;
import com.waters.punchout.gateway.converter.CxmlConversionService;
import com.waters.punchout.gateway.entity.CustomerOnboarding;
import com.waters.punchout.gateway.entity.PunchOutSessionDocument;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.model.PunchOutRequest;
import com.waters.punchout.gateway.repository.PunchOutSessionRepository;
import com.waters.punchout.gateway.service.CustomerOnboardingService;
import com.waters.punchout.gateway.util.EnvironmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PunchOutOrchestrationService {

    private final NetworkRequestLogger networkRequestLogger;
    private final CxmlConversionService cxmlConversionService;
    private final AuthServiceClient authServiceClient;
    private final MuleServiceClient muleServiceClient;
    private final PunchOutSessionRepository sessionRepository;
    private final CustomerOnboardingService onboardingService;
    private final ObjectMapper objectMapper;

    public PunchOutOrchestrationService(
            NetworkRequestLogger networkRequestLogger,
            CxmlConversionService cxmlConversionService,
            AuthServiceClient authServiceClient,
            MuleServiceClient muleServiceClient,
            PunchOutSessionRepository sessionRepository,
            CustomerOnboardingService onboardingService,
            ObjectMapper objectMapper
    ) {
        this.networkRequestLogger = networkRequestLogger;
        this.cxmlConversionService = cxmlConversionService;
        this.authServiceClient = authServiceClient;
        this.muleServiceClient = muleServiceClient;
        this.sessionRepository = sessionRepository;
        this.onboardingService = onboardingService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> processPunchOutRequest(String cxmlContent, String sessionKey) {
        log.info("Processing PunchOut request for sessionKey={}", sessionKey);
        
        PunchOutRequest request = null;
        try {
            // Parse cXML first to extract the session key
            request = convertCxmlToJson(cxmlContent);
            if (sessionKey != null && !sessionKey.isEmpty()) {
                request.setSessionKey(sessionKey);
            }
            
            // Now log the inbound request with the correct session key
            logInboundCxmlRequest(cxmlContent, request.getSessionKey());
            
            String authToken = getAuthenticationToken(request);
            
            Map<String, Object> mulePayload = prepareMulePayload(request);
            
            // Extract environment from request
            String environment = extractEnvironmentFromRequest(request);
            Map<String, Object> muleResponse = getMuleResponse(mulePayload, authToken, request.getSessionKey(), environment);
            
            savePunchOutSession(request, muleResponse);
            
            log.info("Successfully processed PunchOut request for sessionKey={}", request.getSessionKey());
            return buildSuccessResponse(request, muleResponse);
            
        } catch (Exception e) {
            log.error("Error processing PunchOut request for sessionKey={}: {}", sessionKey, e.getMessage(), e);
            
            // Save failed session for troubleshooting
            if (request != null) {
                try {
                    saveFailedPunchOutSession(request, e);
                } catch (Exception saveError) {
                    log.error("Failed to save failed session: {}", saveError.getMessage());
                }
            }
            
            throw new RuntimeException("Failed to process PunchOut request: " + e.getMessage(), e);
        }
    }

    private void logInboundCxmlRequest(String cxmlContent, String sessionKey) {
        log.debug("Logging inbound cXML request");
        
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
        headers.put(HttpHeaders.USER_AGENT, "B2B PunchOut Client");
        headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(cxmlContent != null ? cxmlContent.length() : 0));
        
        networkRequestLogger.logInboundRequest(
                sessionKey != null ? sessionKey : "UNKNOWN",
                "B2B Customer",
                "Punchout Gateway",
                "POST",
                "/punchout/setup",
                headers,
                cxmlContent,
                "cXML"
        );
    }

    private PunchOutRequest convertCxmlToJson(String cxmlContent) {
        try {
            log.debug("Converting cXML using flexible conversion service");
            return cxmlConversionService.convertCxmlToRequest(cxmlContent);
        } catch (Exception e) {
            log.error("Failed to convert cXML: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse cXML request: " + e.getMessage(), e);
        }
    }

    private String getAuthenticationToken(PunchOutRequest request) {
        try {
            log.debug("Obtaining authentication token for sessionKey={}", request.getSessionKey());
            return authServiceClient.getAuthToken(request);
        } catch (Exception e) {
            log.error("Failed to obtain authentication token: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> prepareMulePayload(PunchOutRequest request) {
        log.debug("Preparing Mule payload for sessionKey={}", request.getSessionKey());
        
        // Extract customer identifier from request
        String customerIdentifier = extractCustomerIdentifier(request);
        String environment = extractEnvironmentFromRequest(request);
        
        log.info("Extracted customer identifier: '{}', environment: '{}'", customerIdentifier, environment);
        log.info("Request extrinsics: {}", request.getExtrinsics());
        
        // Try to find onboarded customer configuration
        try {
            List<CustomerOnboarding> onboardings = onboardingService.getOnboardingsByCustomerName(customerIdentifier);
            log.info("Found {} onboarding(s) for customer: {}", onboardings.size(), customerIdentifier);
            
            // Find matching environment
            Optional<CustomerOnboarding> matchingOnboarding = onboardings.stream()
                    .filter(o -> o.getEnvironment().equals(environment) && o.getDeployed())
                    .findFirst();
            
            if (matchingOnboarding.isPresent()) {
                CustomerOnboarding onboarding = matchingOnboarding.get();
                log.info("✅ Found onboarded configuration for customer: {}, environment: {}, onboardingId: {}", 
                        customerIdentifier, environment, onboarding.getId());
                
                // Use the targetJson from onboarding as template
                if (onboarding.getTargetJson() != null && !onboarding.getTargetJson().isEmpty()) {
                    try {
                        log.info("Parsing targetJson (length: {})", onboarding.getTargetJson().length());
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> template = objectMapper.readValue(
                                onboarding.getTargetJson(), 
                                Map.class
                        );
                        
                        log.info("Parsed template keys: {}", template.keySet());
                        
                        // Replace placeholders with actual values from request
                        replaceTemplateValues(template, request);
                        
                        log.info("✅ Using onboarded JSON template for customer: {}, final keys: {}", 
                                customerIdentifier, template.keySet());
                        
                        // Log the actual JSON being sent for debugging
                        try {
                            String jsonString = objectMapper.writeValueAsString(template);
                            log.info("📤 Mule request body: {}", jsonString);
                        } catch (Exception logEx) {
                            log.warn("Could not serialize template for logging");
                        }
                        
                        return template;
                    } catch (Exception e) {
                        log.error("Error parsing targetJson from onboarding: {}", e.getMessage(), e);
                    }
                }
            } else {
                log.warn("No matching onboarding found for customer: {}, environment: {}", 
                        customerIdentifier, environment);
            }
        } catch (Exception e) {
            log.warn("Could not load onboarding config: {}, using default payload", e.getMessage(), e);
        }
        
        // Check if this is Acme customer (buyer123) - use custom converter (legacy)
        if ("buyer123".equals(request.getFromIdentity())) {
            log.debug("Using Acme custom payload builder (legacy)");
            com.waters.punchout.gateway.converter.strategy.customers.AcmeV1Converter acmeConverter = 
                new com.waters.punchout.gateway.converter.strategy.customers.AcmeV1Converter();
            return acmeConverter.buildMulePayload(request);
        }
        
        // Default payload for other customers
        log.debug("Using default payload builder");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionKey", request.getSessionKey());
        payload.put("operation", request.getOperation());
        payload.put("buyerCookie", request.getBuyerCookie());
        payload.put("contactEmail", request.getContactEmail());
        payload.put("cartReturnUrl", request.getCartReturnUrl());
        payload.put("timestamp", request.getTimestamp().toString());
        
        // Include extrinsics if present
        if (request.getExtrinsics() != null) {
            payload.put("extrinsics", request.getExtrinsics());
        }
        
        return payload;
    }

    private String extractCustomerIdentifier(PunchOutRequest request) {
        // Try to get customer name from extrinsics first
        if (request.getExtrinsics() != null) {
            String customerName = request.getExtrinsics().get("CustomerName");
            if (customerName != null && !customerName.isEmpty()) {
                return customerName;
            }
        }
        
        // Fallback to fromIdentity or other identifiers
        if (request.getFromIdentity() != null) {
            return request.getFromIdentity();
        }
        
        return "UNKNOWN";
    }

    @SuppressWarnings("unchecked")
    private void replaceTemplateValues(Map<String, Object> template, PunchOutRequest request) {
        replaceTemplateValues(template, request, true);
    }

    @SuppressWarnings("unchecked")
    private void replaceTemplateValues(Map<String, Object> template, PunchOutRequest request, boolean isRoot) {
        // Recursively replace common placeholder patterns in the template
        for (Map.Entry<String, Object> entry : template.entrySet()) {
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String strValue = (String) value;
                
                // Replace common placeholders
                strValue = strValue.replace("{{sessionKey}}", request.getSessionKey() != null ? request.getSessionKey() : "");
                strValue = strValue.replace("{{buyerCookie}}", request.getBuyerCookie() != null ? request.getBuyerCookie() : "");
                strValue = strValue.replace("{{returnUrl}}", request.getCartReturnUrl() != null ? request.getCartReturnUrl() : "");
                strValue = strValue.replace("{{operation}}", request.getOperation() != null ? request.getOperation() : "create");
                strValue = strValue.replace("{{contactEmail}}", request.getContactEmail() != null ? request.getContactEmail() : "");
                strValue = strValue.replace("{{fromIdentity}}", request.getFromIdentity() != null ? request.getFromIdentity() : "");
                strValue = strValue.replace("{{toIdentity}}", request.getToIdentity() != null ? request.getToIdentity() : "");
                
                entry.setValue(strValue);
            } else if (value instanceof Map) {
                // Recursively process nested maps
                replaceTemplateValues((Map<String, Object>) value, request, false);
            }
        }
        
        // Only add dynamic values to root level (not nested objects)
        if (isRoot) {
            if (!template.containsKey("sessionKey") && request.getSessionKey() != null) {
                template.put("sessionKey", request.getSessionKey());
            }
            if (!template.containsKey("timestamp")) {
                template.put("timestamp", request.getTimestamp().toString());
            }
        }
    }

    private Map<String, Object> getMuleResponse(Map<String, Object> payload, String token, String sessionKey, String environment) {
        try {
            log.debug("Fetching Mule response for sessionKey={}, environment={}", sessionKey, environment);
            return muleServiceClient.sendMuleRequest(payload, token, sessionKey, environment);
        } catch (Exception e) {
            log.error("Failed to get Mule response: {}", e.getMessage(), e);
            throw new RuntimeException("Mule request failed: " + e.getMessage(), e);
        }
    }
    
    private String extractEnvironmentFromRequest(PunchOutRequest request) {
        String rawEnv = null;
        
        // First try to get from extrinsics
        if (request.getExtrinsics() != null && request.getExtrinsics().containsKey("Environment")) {
            rawEnv = request.getExtrinsics().get("Environment");
            log.debug("Extracted environment from extrinsics: {}", rawEnv);
        }
        // Fallback: Extract from session key (e.g., SESSION_DEV_CUST001_123456)
        else if (request.getSessionKey() != null && request.getSessionKey().contains("_")) {
            String[] parts = request.getSessionKey().split("_");
            if (parts.length > 1) {
                rawEnv = parts[1];
                log.debug("Extracted environment from session key: {}", rawEnv);
            }
        }
        
        return EnvironmentUtil.normalize(rawEnv);
    }

    private void savePunchOutSession(PunchOutRequest request, Map<String, Object> muleResponse) {
        log.debug("Saving PunchOut session for sessionKey={}", request.getSessionKey());
        
        PunchOutSessionDocument session = new PunchOutSessionDocument();
        session.setSessionKey(request.getSessionKey());
        session.setBuyerCookie(request.getBuyerCookie());
        session.setOperation(request.getOperation() != null ? request.getOperation().toUpperCase() : "CREATE");
        session.setContact(request.getContactEmail());
        session.setCartReturn(request.getCartReturnUrl());
        session.setSessionDate(LocalDateTime.now());
        session.setPunchedIn(LocalDateTime.now());
        session.setEnvironment(extractEnvironment(request));
        session.setCatalog((String) muleResponse.get("catalogUrl"));
        
        sessionRepository.save(session);
        log.info("Saved PunchOut session: sessionKey={}, environment={}", request.getSessionKey(), session.getEnvironment());
    }

    private void saveFailedPunchOutSession(PunchOutRequest request, Exception error) {
        log.debug("Saving failed PunchOut session for sessionKey={}", request.getSessionKey());
        
        PunchOutSessionDocument session = new PunchOutSessionDocument();
        session.setSessionKey(request.getSessionKey());
        session.setBuyerCookie(request.getBuyerCookie());
        session.setOperation(request.getOperation() != null ? request.getOperation().toUpperCase() : "CREATE");
        session.setContact(request.getContactEmail());
        session.setCartReturn(request.getCartReturnUrl());
        session.setSessionDate(LocalDateTime.now());
        session.setPunchedIn(LocalDateTime.now());
        session.setEnvironment(extractEnvironment(request));
        session.setCatalog("FAILED: " + error.getMessage());
        
        sessionRepository.save(session);
        log.info("Saved failed PunchOut session: sessionKey={}, error={}", request.getSessionKey(), error.getMessage());
    }

    private String extractEnvironment(PunchOutRequest request) {
        String rawEnv = null;
        
        // First try to get from extrinsics
        if (request.getExtrinsics() != null && request.getExtrinsics().containsKey("Environment")) {
            rawEnv = request.getExtrinsics().get("Environment");
        }
        // Fallback: Extract environment from session key if available (e.g., SESSION_DEV_CUST001_123456)
        else if (request.getSessionKey() != null) {
            String[] parts = request.getSessionKey().split("_");
            if (parts.length > 1) {
                rawEnv = parts[1];
            }
        }
        
        String normalizedEnv = EnvironmentUtil.normalize(rawEnv);
        return mapEnvironmentName(normalizedEnv);
    }

    private String mapEnvironmentName(String env) {
        if (env == null) return "DEVELOPMENT";
        
        switch (env.toLowerCase()) {
            case "dev":
                return "DEVELOPMENT";
            case "stage":
                return "STAGING";
            case "prod":
                return "PRODUCTION";
            case "s4":
            case "s4-dev":
                return "S4_DEVELOPMENT";
            default:
                return "DEVELOPMENT";
        }
    }

    private Map<String, Object> buildSuccessResponse(PunchOutRequest request, Map<String, Object> muleResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionKey", request.getSessionKey());
        response.put("catalogUrl", muleResponse.get("catalogUrl"));
        response.put("message", "PunchOut session established successfully");
        return response;
    }
    
    public Map<String, Object> processOrderMessage(String cxmlContent) {
        long startTime = System.currentTimeMillis();
        String requestId = "REQ_ORDER_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        
        try {
            log.info("Processing order message request: {}", requestId);
            
            String sessionKey = extractSessionKeyFromOrderMessage(cxmlContent);
            
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            
            networkRequestLogger.logInboundRequest(
                    sessionKey,
                    "B2B Customer",
                    "Punchout Gateway",
                    "POST",
                    "/punchout/order",
                    headers,
                    cxmlContent,
                    "cXML"
            );
            
            log.info("Order message processed for session: {}", sessionKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionKey", sessionKey);
            result.put("status", "success");
            result.put("duration", System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error processing order message: {}", requestId, e);
            throw new RuntimeException("Failed to process order message: " + e.getMessage(), e);
        }
    }
    
    private String extractSessionKeyFromOrderMessage(String cxmlContent) {
        return "SESSION_ORDER_" + System.currentTimeMillis();
    }
}
