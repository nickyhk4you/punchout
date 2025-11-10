package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.client.AuthServiceClient;
import com.waters.punchout.gateway.client.MuleServiceClient;
import com.waters.punchout.gateway.converter.CxmlConversionService;
import com.waters.punchout.gateway.entity.PunchOutSessionDocument;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.model.PunchOutRequest;
import com.waters.punchout.gateway.repository.PunchOutSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PunchOutOrchestrationService {

    private final NetworkRequestLogger networkRequestLogger;
    private final CxmlConversionService cxmlConversionService;
    private final AuthServiceClient authServiceClient;
    private final MuleServiceClient muleServiceClient;
    private final PunchOutSessionRepository sessionRepository;

    public PunchOutOrchestrationService(
            NetworkRequestLogger networkRequestLogger,
            CxmlConversionService cxmlConversionService,
            AuthServiceClient authServiceClient,
            MuleServiceClient muleServiceClient,
            PunchOutSessionRepository sessionRepository
    ) {
        this.networkRequestLogger = networkRequestLogger;
        this.cxmlConversionService = cxmlConversionService;
        this.authServiceClient = authServiceClient;
        this.muleServiceClient = muleServiceClient;
        this.sessionRepository = sessionRepository;
    }

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
            
            Map<String, Object> mulePayload = prepareMulePayload(request);
            
            Map<String, Object> muleResponse = getMuleResponse(mulePayload, authToken, request.getSessionKey());
            
            savePunchOutSession(request, muleResponse);
            
            log.info("Successfully processed PunchOut request for sessionKey={}", request.getSessionKey());
            return buildSuccessResponse(request, muleResponse);
            
        } catch (Exception e) {
            log.error("Error processing PunchOut request for sessionKey={}: {}", sessionKey, e.getMessage(), e);
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
        log.debug("Preparing Mule payload");
        
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

    private Map<String, Object> getMuleResponse(Map<String, Object> payload, String token, String sessionKey) {
        try {
            log.debug("Fetching Mule response for sessionKey={}", sessionKey);
            return muleServiceClient.sendMuleRequest(payload, token, sessionKey);
        } catch (Exception e) {
            log.error("Failed to get Mule response: {}", e.getMessage(), e);
            throw new RuntimeException("Mule request failed: " + e.getMessage(), e);
        }
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
        session.setCatalog((String) muleResponse.get("catalogUrl"));
        
        sessionRepository.save(session);
        log.info("Saved PunchOut session: sessionKey={}, environment={}", request.getSessionKey(), environment);
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
