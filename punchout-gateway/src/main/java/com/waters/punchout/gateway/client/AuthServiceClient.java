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
        return getAuthToken(request, currentEnvironment);
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
            
            String token = responseEntity.getBody();
            statusCode = responseEntity.getStatusCode().value();
            
            // Capture all response headers
            responseEntity.getHeaders().forEach((name, values) -> {
                responseHeadersMap.put(name, String.join(", ", values));
            });
            
            responseBody = token;
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
        payload.put("sessionKey", request.getSessionKey());
        payload.put("operation", request.getOperation());
        payload.put("buyerCookie", request.getBuyerCookie());
        payload.put("fromIdentity", request.getFromIdentity());
        payload.put("toIdentity", request.getToIdentity());
        payload.put("senderIdentity", request.getSenderIdentity());
        return payload;
    }
}
