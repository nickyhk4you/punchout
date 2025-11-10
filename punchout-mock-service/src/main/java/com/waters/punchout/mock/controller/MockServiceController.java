package com.waters.punchout.mock.controller;

import com.waters.punchout.mock.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MockServiceController {
    
    private final TokenService tokenService;
    
    @PostMapping("/token")
    public ResponseEntity<String> generateToken(@RequestBody Map<String, Object> request) {
        log.info("Mock Auth Service: Received token request for sessionKey={}", 
                request.get("sessionKey"));
        
        String sessionKey = (String) request.get("sessionKey");
        String operation = (String) request.get("operation");
        
        // Generate one-time token
        String token = tokenService.generateOneTimeToken(sessionKey, operation);
        
        log.info("Mock Auth Service: Generated one-time token: {}", token);
        
        return ResponseEntity.ok(token);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        log.info("Mock Auth Service: Validating token: {}", token);
        
        boolean isValid = tokenService.validateAndConsumeToken(token);
        
        return ResponseEntity.ok(Map.of(
            "valid", isValid,
            "message", isValid ? "Token is valid" : "Token is invalid or already used"
        ));
    }
    
    @GetMapping("/service-health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "mock-service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
