package com.waters.punchout.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.model.PunchOutRequest;
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
    private final String authUrl;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            NetworkRequestLogger networkRequestLogger,
            ObjectMapper objectMapper,
            @Value("${thirdparty.auth.url}") String authUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.networkRequestLogger = networkRequestLogger;
        this.objectMapper = objectMapper;
        this.authUrl = authUrl;
    }

    public String getAuthToken(PunchOutRequest request) {
        log.info("Requesting auth token for sessionKey={}", request.getSessionKey());
        
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
            
            String token = webClient.post()
                    .uri(authUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            statusCode = 200;
            responseBody = token;
            success = true;
            
            log.info("Successfully obtained auth token for sessionKey={}", request.getSessionKey());
            return token;
            
        } catch (WebClientResponseException e) {
            statusCode = e.getStatusCode().value();
            responseBody = e.getResponseBodyAsString();
            errorMessage = "Auth service error: " + e.getMessage();
            log.error("Failed to get auth token: statusCode={}, error={}", statusCode, errorMessage);
            throw new RuntimeException("Failed to get auth token: " + e.getMessage(), e);
            
        } catch (Exception e) {
            errorMessage = "Unexpected error: " + e.getMessage();
            log.error("Unexpected error while getting auth token: {}", errorMessage, e);
            throw new RuntimeException("Failed to get auth token: " + e.getMessage(), e);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            
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
                    success,
                    errorMessage
            );
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
