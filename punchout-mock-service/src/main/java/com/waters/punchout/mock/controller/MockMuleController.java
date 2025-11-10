package com.waters.punchout.mock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@CrossOrigin(origins = "*")
public class MockMuleController {
    
    @Value("${catalog.base-url:http://localhost:3000/catalog}")
    private String catalogBaseUrl;
    
    @PostMapping("/catalog")
    public ResponseEntity<Map<String, Object>> getCatalog(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request) {
        
        String sessionKey = (String) request.get("sessionKey");
        log.info("Mock Catalog Service: Received catalog request for sessionKey={}", sessionKey);
        
        // Validate authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Mock Catalog Service: Missing or invalid authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid authorization token"));
        }
        
        String token = authHeader.substring(7);
        log.info("Mock Catalog Service: Using token: {}", token);
        
        // Build catalog URL with session key
        String catalogUrl = catalogBaseUrl + "?sessionKey=" + sessionKey;
        
        Map<String, Object> response = Map.of(
            "catalogUrl", catalogUrl,
            "sessionKey", sessionKey,
            "status", "success",
            "message", "Catalog initialized successfully"
        );
        
        log.info("Mock Catalog Service: Returning catalog URL: {}", catalogUrl);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mule-health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "mock-mule-service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
