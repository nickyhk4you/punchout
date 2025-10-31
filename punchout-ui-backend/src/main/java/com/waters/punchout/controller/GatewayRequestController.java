package com.waters.punchout.controller;

import com.waters.punchout.dto.GatewayRequestDTO;
import com.waters.punchout.service.GatewayRequestService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayRequestController {
    
    private final GatewayRequestService gatewayRequestService;
    
    @GetMapping("/api/punchout-sessions/{sessionKey}/gateway-requests")
    public ResponseEntity<List<GatewayRequestDTO>> getGatewayRequests(@PathVariable String sessionKey) {
        log.info("GET /api/punchout-sessions/{}/gateway-requests - Fetching gateway requests", sessionKey);
        
        List<GatewayRequestDTO> requests = gatewayRequestService.getGatewayRequestsBySessionKey(sessionKey);
        return ResponseEntity.ok(requests);
    }
    
    @PostMapping("/api/gateway-requests")
    public ResponseEntity<GatewayRequestDTO> createGatewayRequest(@Valid @RequestBody GatewayRequestDTO gatewayRequestDTO) {
        log.info("POST /api/gateway-requests - Creating new gateway request");
        
        GatewayRequestDTO createdRequest = gatewayRequestService.createGatewayRequest(gatewayRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }
}
