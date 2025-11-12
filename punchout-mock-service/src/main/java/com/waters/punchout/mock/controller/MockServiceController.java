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
    
    @PostMapping("/waters/user/v2/login")
    public ResponseEntity<Map<String, Object>> watersLogin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        log.info("Mock Waters Auth Service: Login attempt for email={}", email);
        
        // Validate credentials (mock validation)
        if ("USMulti2@yopmail.com".equals(email) && "Password1!".equals(password)) {
            String token = tokenService.generateOneTimeToken(email, "login");
            
            Map<String, Object> response = Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "email", email,
                    "name", "Test User",
                    "userId", "USMulti2"
                )
            );
            
            log.info("Mock Waters Auth Service: Login successful, token={}", token);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Mock Waters Auth Service: Invalid credentials for email={}", email);
            return ResponseEntity.status(401)
                .body(Map.of(
                    "success", false,
                    "error", "Invalid email or password"
                ));
        }
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
