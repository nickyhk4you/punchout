package com.waters.punchout.gateway.logging;

import com.waters.punchout.gateway.repository.NetworkRequestRepository;
import com.waters.punchout.gateway.entity.NetworkRequestDocument;
import com.waters.punchout.gateway.service.EnvironmentConfigService;
import com.waters.punchout.gateway.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NetworkRequestLogger {

    private final NetworkRequestRepository networkRequestRepository;
    private final EnvironmentConfigService environmentConfigService;

    public NetworkRequestLogger(NetworkRequestRepository networkRequestRepository,
                                EnvironmentConfigService environmentConfigService) {
        this.networkRequestRepository = networkRequestRepository;
        this.environmentConfigService = environmentConfigService;
    }
    
    private String maskIfRequired(String body, String environment) {
        if (environment != null && !environmentConfigService.shouldMaskSensitiveData(environment)) {
            return body;
        }
        return SecurityUtil.maskSecrets(body);
    }
    
    private Map<String, String> maskHeadersIfRequired(Map<String, String> headers, String environment) {
        if (environment != null && !environmentConfigService.shouldMaskSensitiveData(environment)) {
            return headers;
        }
        return SecurityUtil.maskHeaders(headers);
    }
    
    private String extractEnvironmentFromSessionKey(String sessionKey) {
        if (sessionKey == null || !sessionKey.startsWith("SESSION_")) {
            return null;
        }
        // Format: SESSION_{ENV}_{customer}_{env}_{id}_{timestamp}
        // e.g., SESSION_PROD_tradecentric_prod_IC692112fbc6691_1765630061164
        String[] parts = sessionKey.split("_");
        if (parts.length >= 2) {
            return parts[1].toLowerCase();
        }
        return null;
    }
    
    public static class OrderContext {
        private final String orderId;
        
        public OrderContext(String orderId) {
            this.orderId = orderId;
        }
        
        public String getOrderId() {
            return orderId;
        }
    }

    public NetworkRequestDocument logInboundRequest(
            String sessionKey,
            String source,
            String destination,
            String method,
            String url,
            Map<String, String> headers,
            String requestBody,
            String requestType
    ) {
        log.debug("Logging inbound request for sessionKey={}", sessionKey);
        
        String environment = extractEnvironmentFromSessionKey(sessionKey);

        NetworkRequestDocument document = new NetworkRequestDocument();
        document.setRequestId(generateRequestId());
        document.setSessionKey(sessionKey);
        document.setTimestamp(LocalDateTime.now());
        document.setDirection("INBOUND");
        document.setSource(source);
        document.setDestination(destination);
        document.setMethod(method);
        document.setUrl(url);
        document.setHeaders(maskHeadersIfRequired(headers, environment));
        document.setRequestBody(maskIfRequired(requestBody, environment));
        document.setRequestType(requestType);

        NetworkRequestDocument saved = networkRequestRepository.save(document);
        log.info("Logged inbound request: requestId={}, sessionKey={}", saved.getRequestId(), sessionKey);
        return saved;
    }

    public NetworkRequestDocument logOutboundRequest(
            String sessionKey,
            String source,
            String destination,
            String method,
            String url,
            Map<String, String> headers,
            String requestBody,
            Integer statusCode,
            Map<String, String> responseHeaders,
            String responseBody,
            Long duration,
            String requestType,
            Boolean success,
            String errorMessage
    ) {
        log.debug("Logging outbound request for sessionKey={}", sessionKey);
        
        String environment = extractEnvironmentFromSessionKey(sessionKey);

        NetworkRequestDocument document = new NetworkRequestDocument();
        document.setRequestId(generateRequestId());
        document.setSessionKey(sessionKey);
        document.setTimestamp(LocalDateTime.now());
        document.setDirection("OUTBOUND");
        document.setSource(source);
        document.setDestination(destination);
        document.setMethod(method);
        document.setUrl(url);
        document.setHeaders(maskHeadersIfRequired(headers, environment));
        document.setRequestBody(maskIfRequired(requestBody, environment));
        document.setStatusCode(statusCode);
        document.setResponseHeaders(maskHeadersIfRequired(responseHeaders, environment));
        document.setResponseBody(maskIfRequired(responseBody, environment));
        document.setDuration(duration);
        document.setRequestType(requestType);
        document.setSuccess(success);
        document.setErrorMessage(errorMessage);

        NetworkRequestDocument saved = networkRequestRepository.save(document);
        log.info("Logged outbound request: requestId={}, sessionKey={}, success={}", 
                saved.getRequestId(), sessionKey, success);
        return saved;
    }

    public void updateRequestResponse(
            String requestId,
            Integer statusCode,
            Map<String, String> responseHeaders,
            String responseBody,
            Long duration,
            Boolean success,
            String errorMessage
    ) {
        networkRequestRepository.findById(requestId).ifPresent(document -> {
            String environment = extractEnvironmentFromSessionKey(document.getSessionKey());
            document.setStatusCode(statusCode);
            document.setResponseHeaders(maskHeadersIfRequired(responseHeaders, environment));
            document.setResponseBody(maskIfRequired(responseBody, environment));
            document.setDuration(duration);
            document.setSuccess(success);
            document.setErrorMessage(errorMessage);
            networkRequestRepository.save(document);
            log.debug("Updated request response: requestId={}", requestId);
        });
    }

    public NetworkRequestDocument logInboundOrderRequest(
            String sessionKey,
            String orderId,
            String source,
            String destination,
            String method,
            String url,
            Map<String, String> headers,
            String requestBody,
            String requestType
    ) {
        log.debug("Logging inbound order request for orderId={}", orderId);
        
        String environment = extractEnvironmentFromSessionKey(sessionKey);

        NetworkRequestDocument document = new NetworkRequestDocument();
        document.setRequestId(generateRequestId());
        document.setSessionKey(sessionKey);
        document.setOrderId(orderId);
        document.setTimestamp(LocalDateTime.now());
        document.setDirection("INBOUND");
        document.setSource(source);
        document.setDestination(destination);
        document.setMethod(method);
        document.setUrl(url);
        document.setHeaders(maskHeadersIfRequired(headers, environment));
        document.setRequestBody(maskIfRequired(requestBody, environment));
        document.setRequestType(requestType);

        NetworkRequestDocument saved = networkRequestRepository.save(document);
        log.info("Logged inbound order request: requestId={}, orderId={}", saved.getRequestId(), orderId);
        return saved;
    }
    
    private String generateRequestId() {
        return "REQ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
