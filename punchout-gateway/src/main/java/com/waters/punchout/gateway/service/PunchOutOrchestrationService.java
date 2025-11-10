package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.client.AuthServiceClient;
import com.waters.punchout.gateway.client.CatalogServiceClient;
import com.waters.punchout.gateway.converter.CxmlToJsonConverter;
import com.waters.punchout.gateway.entity.PunchOutSessionDocument;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.model.PunchOutRequest;
import com.waters.punchout.gateway.repository.PunchOutSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PunchOutOrchestrationService {

    private final NetworkRequestLogger networkRequestLogger;
    private final CxmlToJsonConverter cxmlToJsonConverter;
    private final AuthServiceClient authServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final PunchOutSessionRepository sessionRepository;

    public PunchOutOrchestrationService(
            NetworkRequestLogger networkRequestLogger,
            CxmlToJsonConverter cxmlToJsonConverter,
            AuthServiceClient authServiceClient,
            CatalogServiceClient catalogServiceClient,
            PunchOutSessionRepository sessionRepository
    ) {
        this.networkRequestLogger = networkRequestLogger;
        this.cxmlToJsonConverter = cxmlToJsonConverter;
        this.authServiceClient = authServiceClient;
        this.catalogServiceClient = catalogServiceClient;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Map<String, Object> processPunchOutRequest(String cxmlContent, String sessionKey) {
        log.info("Processing PunchOut request for sessionKey={}", sessionKey);
        
        try {
            // Parse cXML first to extract the session key
            PunchOutRequest request = convertCxmlToJson(cxmlContent);
            if (sessionKey != null && !sessionKey.isEmpty()) {
                request.setSessionKey(sessionKey);
            }
            
            // Now log the inbound request with the correct session key
            logInboundCxmlRequest(cxmlContent, request.getSessionKey());
            
            String authToken = getAuthenticationToken(request);
            
            Map<String, Object> catalogPayload = prepareCatalogPayload(request);
            
            Map<String, Object> catalogResponse = getCatalogResponse(catalogPayload, authToken, request.getSessionKey());
            
            savePunchOutSession(request, catalogResponse);
            
            log.info("Successfully processed PunchOut request for sessionKey={}", request.getSessionKey());
            return buildSuccessResponse(request, catalogResponse);
            
        } catch (Exception e) {
            log.error("Error processing PunchOut request for sessionKey={}: {}", sessionKey, e.getMessage(), e);
            throw new RuntimeException("Failed to process PunchOut request: " + e.getMessage(), e);
        }
    }

    private void logInboundCxmlRequest(String cxmlContent, String sessionKey) {
        log.debug("Logging inbound cXML request");
        
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        
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
            log.debug("Converting cXML to JSON");
            return cxmlToJsonConverter.convertCxmlToRequest(cxmlContent);
        } catch (Exception e) {
            log.error("Failed to convert cXML to JSON: {}", e.getMessage(), e);
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

    private Map<String, Object> prepareCatalogPayload(PunchOutRequest request) {
        log.debug("Preparing catalog payload");
        return cxmlToJsonConverter.convertToThirdPartyPayload(request);
    }

    private Map<String, Object> getCatalogResponse(Map<String, Object> payload, String token, String sessionKey) {
        try {
            log.debug("Fetching catalog response for sessionKey={}", sessionKey);
            return catalogServiceClient.sendCatalogRequest(payload, token, sessionKey);
        } catch (Exception e) {
            log.error("Failed to get catalog response: {}", e.getMessage(), e);
            throw new RuntimeException("Catalog request failed: " + e.getMessage(), e);
        }
    }

    private void savePunchOutSession(PunchOutRequest request, Map<String, Object> catalogResponse) {
        log.debug("Saving PunchOut session for sessionKey={}", request.getSessionKey());
        
        PunchOutSessionDocument session = new PunchOutSessionDocument();
        session.setSessionKey(request.getSessionKey());
        session.setBuyerCookie(request.getBuyerCookie());
        session.setOperation(request.getOperation() != null ? request.getOperation().toUpperCase() : "CREATE");
        session.setContact(request.getContactEmail());
        session.setCartReturn(request.getCartReturnUrl());
        session.setSessionDate(LocalDateTime.now());
        session.setPunchedIn(LocalDateTime.now());
        
        // Extract environment from session key if available (e.g., SESSION_DEV_CUST001_123456)
        String environment = "DEVELOPMENT";
        if (request.getSessionKey() != null) {
            String[] parts = request.getSessionKey().split("_");
            if (parts.length > 1) {
                String envPart = parts[1];
                // Map to expected values: dev -> DEVELOPMENT, prod -> PRODUCTION, etc.
                switch (envPart.toLowerCase()) {
                    case "dev":
                        environment = "DEVELOPMENT";
                        break;
                    case "stage":
                        environment = "STAGING";
                        break;
                    case "prod":
                        environment = "PRODUCTION";
                        break;
                    case "s4":
                    case "s4-dev":
                        environment = "S4_DEVELOPMENT";
                        break;
                    default:
                        environment = "DEVELOPMENT";
                }
            }
        }
        session.setEnvironment(environment);
        session.setCatalog((String) catalogResponse.get("catalogUrl"));
        
        sessionRepository.save(session);
        log.info("Saved PunchOut session: sessionKey={}, environment={}", request.getSessionKey(), environment);
    }

    private Map<String, Object> buildSuccessResponse(PunchOutRequest request, Map<String, Object> catalogResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionKey", request.getSessionKey());
        response.put("catalogUrl", catalogResponse.get("catalogUrl"));
        response.put("message", "PunchOut session established successfully");
        return response;
    }
    
    @Transactional
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
