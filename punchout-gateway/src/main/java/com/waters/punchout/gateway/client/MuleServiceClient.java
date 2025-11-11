package com.waters.punchout.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
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
public class MuleServiceClient {

    private final WebClient webClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final ObjectMapper objectMapper;
    private final EnvironmentConfigService environmentConfigService;
    
    @Value("${app.environment:dev}")
    private String currentEnvironment;

    public MuleServiceClient(
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

    public Map<String, Object> sendMuleRequest(Map<String, Object> payload, String token, String sessionKey) {
        return sendMuleRequest(payload, token, sessionKey, currentEnvironment);
    }

    public Map<String, Object> sendMuleRequest(Map<String, Object> payload, String token, String sessionKey, String environment) {
        String muleUrl = environmentConfigService.getMuleServiceUrl(environment);
        log.info("Sending Mule request for sessionKey={}, environment={}, url={}", 
                sessionKey, environment, muleUrl);
        
        long startTime = System.currentTimeMillis();
        String requestBody = null;
        String responseBody = null;
        Integer statusCode = null;
        boolean success = false;
        String errorMessage = null;
        
        // Prepare request headers (outside try block for use in catch)
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        requestHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        
        try {
            requestBody = objectMapper.writeValueAsString(payload);
            
            Map<String, String> responseHeadersMap = new HashMap<>();
            
            var responseEntity = webClient.post()
                    .uri(muleUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(payload)
                    .retrieve()
                    .toEntity(Map.class)
                    .block();
            
            Map<String, Object> response = responseEntity.getBody();
            statusCode = responseEntity.getStatusCode().value();
            
            // Capture response headers
            responseEntity.getHeaders().forEach((name, values) -> {
                responseHeadersMap.put(name, String.join(", ", values));
            });
            
            responseBody = objectMapper.writeValueAsString(response);
            success = true;
            
            log.info("Successfully received catalog response for sessionKey={}", sessionKey);
            
            // Log with response headers
            networkRequestLogger.logOutboundRequest(
                    sessionKey,
                    "Punchout Gateway",
                    "Catalog Service",
                    "POST",
                    muleUrl,
                    requestHeaders,
                    requestBody,
                    statusCode,
                    responseHeadersMap,
                    responseBody,
                    System.currentTimeMillis() - startTime,
                    "REST",
                    success,
                    errorMessage
            );
            
            return response;
            
        } catch (WebClientResponseException e) {
            statusCode = e.getStatusCode().value();
            responseBody = e.getResponseBodyAsString();
            errorMessage = "Mule service error: " + e.getMessage();
            
            // Capture error response headers
            Map<String, String> errorResponseHeaders = new HashMap<>();
            e.getHeaders().forEach((name, values) -> {
                errorResponseHeaders.put(name, String.join(", ", values));
            });
            
            long duration = System.currentTimeMillis() - startTime;
            networkRequestLogger.logOutboundRequest(
                    sessionKey,
                    "Punchout Gateway",
                    "Mule Service",
                    "POST",
                    muleUrl,
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
            
            log.error("Failed to get Mule response: statusCode={}, error={}", statusCode, errorMessage);
            throw new RuntimeException("Failed to get Mule response: " + e.getMessage(), e);
            
        } catch (Exception e) {
            errorMessage = "Unexpected error: " + e.getMessage();
            
            long duration = System.currentTimeMillis() - startTime;
            networkRequestLogger.logOutboundRequest(
                    sessionKey,
                    "Punchout Gateway",
                    "Mule Service",
                    "POST",
                    muleUrl,
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
            
            log.error("Unexpected error while getting Mule response: {}", errorMessage, e);
            throw new RuntimeException("Failed to get Mule response: " + e.getMessage(), e);
        }
    }
}
