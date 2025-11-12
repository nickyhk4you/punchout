package com.waters.punchout.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.model.PunchOutRequest;
import com.waters.punchout.gateway.service.EnvironmentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final ObjectMapper objectMapper;
    private final EnvironmentConfigService environmentConfigService;
    
    @Value("${app.environment:dev}")
    private String currentEnvironment;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            NetworkRequestLogger networkRequestLogger,
            ObjectMapper objectMapper,
            EnvironmentConfigService environmentConfigService
    ) {
        this.webClient = webClientBuilder.build();
        this.networkRequestLogger = networkRequestLogger;
        this.objectMapper = objectMapper;
        this.environmentConfigService = environmentConfigService;
    }

    public String getAuthToken(PunchOutRequest request) {
        // Extract environment from request extrinsics, fallback to current environment
        String environment = currentEnvironment;
        if (request.getExtrinsics() != null && request.getExtrinsics().containsKey("Environment")) {
            environment = request.getExtrinsics().get("Environment");
            log.info("Using environment from request extrinsics: {}", environment);
        } else {
            log.info("No environment in request extrinsics, using current environment: {}", environment);
        }
        return getAuthToken(request, environment);
    }

    public String getAuthToken(PunchOutRequest request, String environment) {
        String authUrl = environmentConfigService.getAuthServiceUrl(environment);
        log.info("Requesting auth token for sessionKey={}, environment={}, url={}", 
                request.getSessionKey(), environment, authUrl);
        
        long startTime = System.currentTimeMillis();
        String requestBody = null;
        String responseBody = null;
        Integer statusCode = null;
        boolean success = false;
        String errorMessage = null;
        
        try {
            Map<String, Object> payload = buildAuthPayload(request);
            requestBody = objectMapper.writeValueAsString(payload);
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            requestHeaders.put(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
            
            Map<String, String> responseHeadersMap = new HashMap<>();
            
            var responseEntity = webClient.post()
                    .uri(authUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            
            statusCode = responseEntity.getStatusCode().value();
            responseBody = responseEntity.getBody(); // Keep original body for logging
            
            // Capture all response headers
            responseEntity.getHeaders().forEach((name, values) -> {
                responseHeadersMap.put(name, String.join(", ", values));
            });
            
            // Extract wuser_key from Set-Cookie header (for Waters auth service)
            String token = null;
            if (responseEntity.getHeaders().containsKey("Set-Cookie")) {
                List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
                if (cookies != null) {
                    for (String cookie : cookies) {
                        if (cookie.startsWith("wuser_key=")) {
                            // Extract value: wuser_key=VALUE; Path=/; Domain=...
                            token = cookie.substring("wuser_key=".length()).split(";")[0];
                            log.info("Extracted wuser_key from Set-Cookie header for Waters auth");
                            break;
                        }
                    }
                }
            }
            
            if (token == null) {
                // Fallback: use response body as token (for legacy/mock services)
                token = responseBody;
                log.info("Using response body as token (legacy mode)");
            }
            
            success = true;
            
            log.info("Successfully obtained auth token for sessionKey={}", request.getSessionKey());
            
            // Log successful request with complete headers
            long duration = System.currentTimeMillis() - startTime;
            networkRequestLogger.logOutboundRequest(
                    request.getSessionKey(),
                    "Punchout Gateway",
                    "Auth Service",
                    "POST",
                    authUrl,
                    requestHeaders,
                    requestBody,
                    statusCode,
                    responseHeadersMap,
                    responseBody,
                    duration,
                    "REST",
                    success,
                    errorMessage
            );
            
            return token;
            
        } catch (WebClientResponseException e) {
            statusCode = e.getStatusCode().value();
            responseBody = e.getResponseBodyAsString();
            errorMessage = "Auth service error: " + e.getMessage();
            
            // Capture error response headers
            Map<String, String> errorResponseHeaders = new HashMap<>();
            e.getHeaders().forEach((name, values) -> {
                errorResponseHeaders.put(name, String.join(", ", values));
            });
            
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            requestHeaders.put(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
            
            networkRequestLogger.logOutboundRequest(
                    request.getSessionKey(),
                    "Punchout Gateway",
                    "Auth Service",
                    "POST",
                    authUrl,
                    requestHeaders,
                    requestBody,
                    statusCode,
                    errorResponseHeaders,
                    responseBody,
                    duration,
                    "REST",
                    false,
                    errorMessage
            );
            
            log.error("Failed to get auth token: statusCode={}, error={}", statusCode, errorMessage);
            throw new RuntimeException("Failed to get auth token: " + e.getMessage(), e);
            
        } catch (Exception e) {
            errorMessage = "Unexpected error: " + e.getMessage();
            
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            requestHeaders.put(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
            
            networkRequestLogger.logOutboundRequest(
                    request.getSessionKey(),
                    "Punchout Gateway",
                    "Auth Service",
                    "POST",
                    authUrl,
                    requestHeaders,
                    requestBody,
                    statusCode,
                    null,
                    responseBody,
                    duration,
                    "REST",
                    false,
                    errorMessage
            );
            
            log.error("Unexpected error while getting auth token: {}", errorMessage, e);
            throw new RuntimeException("Failed to get auth token: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildAuthPayload(PunchOutRequest request) {
        Map<String, Object> payload = new HashMap<>();
        
        // Check if environment uses Waters auth service (dev/stage/prod)
        String authUrl = environmentConfigService.getAuthServiceUrl(currentEnvironment);
        
        if (authUrl != null && authUrl.contains("waters.com")) {
            // Waters auth service format - use email/password
            payload.put("email", "USMulti2@yopmail.com");
            payload.put("password", "Password1!");
        } else {
            // Legacy/local format - use session key
            payload.put("sessionKey", request.getSessionKey());
            payload.put("operation", request.getOperation());
            payload.put("buyerCookie", request.getBuyerCookie());
            payload.put("fromIdentity", request.getFromIdentity());
            payload.put("toIdentity", request.getToIdentity());
            payload.put("senderIdentity", request.getSenderIdentity());
        }
        
        return payload;
    }
}
