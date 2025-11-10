package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.NetworkRequestDTO;
import com.waters.punchout.mongo.service.NetworkRequestMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NetworkRequestMongoController {
    
    private final NetworkRequestMongoService networkRequestService;
    
    @GetMapping("/sessions/{sessionKey}/network-requests")
    public ResponseEntity<List<NetworkRequestDTO>> getNetworkRequestsBySession(@PathVariable String sessionKey) {
        log.info("GET /api/v1/sessions/{}/network-requests - Fetching network requests", sessionKey);
        
        List<NetworkRequestDTO> requests = networkRequestService.getNetworkRequestsBySessionKey(sessionKey);
        
        log.info("Returning {} network requests", requests.size());
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/sessions/{sessionKey}/network-requests/inbound")
    public ResponseEntity<List<NetworkRequestDTO>> getInboundRequests(@PathVariable String sessionKey) {
        log.info("GET /api/v1/sessions/{}/network-requests/inbound", sessionKey);
        
        List<NetworkRequestDTO> requests = networkRequestService.getNetworkRequestsBySessionKeyAndDirection(sessionKey, "INBOUND");
        
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/sessions/{sessionKey}/network-requests/outbound")
    public ResponseEntity<List<NetworkRequestDTO>> getOutboundRequests(@PathVariable String sessionKey) {
        log.info("GET /api/v1/sessions/{}/network-requests/outbound", sessionKey);
        
        List<NetworkRequestDTO> requests = networkRequestService.getNetworkRequestsBySessionKeyAndDirection(sessionKey, "OUTBOUND");
        
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/network-requests/{id}")
    public ResponseEntity<NetworkRequestDTO> getNetworkRequestById(@PathVariable String id) {
        log.info("GET /api/v1/network-requests/{} - Fetching network request details", id);
        
        NetworkRequestDTO request = networkRequestService.getNetworkRequestById(id);
        
        return ResponseEntity.ok(request);
    }
}
