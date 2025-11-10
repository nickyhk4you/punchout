package com.waters.punchout.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
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
public class CatalogServiceClient {

    private final WebClient webClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final ObjectMapper objectMapper;
    private final String catalogUrl;

    public CatalogServiceClient(
            WebClient.Builder webClientBuilder,
            NetworkRequestLogger networkRequestLogger,
            ObjectMapper objectMapper,
            @Value("${thirdparty.catalog.url}") String catalogUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.networkRequestLogger = networkRequestLogger;
        this.objectMapper = objectMapper;
        this.catalogUrl = catalogUrl;
    }

    public Map<String, Object> sendCatalogRequest(Map<String, Object> payload, String token, String sessionKey) {
        log.info("Sending catalog request for sessionKey={}", sessionKey);
        
        long startTime = System.currentTimeMillis();
        String requestBody = null;
        String responseBody = null;
        Integer statusCode = null;
        boolean success = false;
        String errorMessage = null;
        
        try {
            requestBody = objectMapper.writeValueAsString(payload);
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            requestHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            
            Map<String, Object> response = webClient.post()
                    .uri(catalogUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            statusCode = 200;
            responseBody = objectMapper.writeValueAsString(response);
            success = true;
            
            log.info("Successfully received catalog response for sessionKey={}", sessionKey);
            return response;
            
        } catch (WebClientResponseException e) {
            statusCode = e.getStatusCode().value();
            responseBody = e.getResponseBodyAsString();
            errorMessage = "Catalog service error: " + e.getMessage();
            log.error("Failed to get catalog response: statusCode={}, error={}", statusCode, errorMessage);
            throw new RuntimeException("Failed to get catalog response: " + e.getMessage(), e);
            
        } catch (Exception e) {
            errorMessage = "Unexpected error: " + e.getMessage();
            log.error("Unexpected error while getting catalog response: {}", errorMessage, e);
            throw new RuntimeException("Failed to get catalog response: " + e.getMessage(), e);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            requestHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            
            networkRequestLogger.logOutboundRequest(
                    sessionKey,
                    "Punchout Gateway",
                    "Catalog Service",
                    "POST",
                    catalogUrl,
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
}
