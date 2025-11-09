package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.mongo.service.PunchOutSessionMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SessionMongoController {
    
    private final PunchOutSessionMongoService sessionService;
    
    @GetMapping
    public ResponseEntity<List<PunchOutSessionDTO>> getAllSessions() {
        log.info("GET /api/v1/sessions - Fetching all sessions");
        
        List<PunchOutSessionDTO> sessions = sessionService.getAllSessions();
        
        log.info("Returning {} sessions", sessions.size());
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/{sessionKey}")
    public ResponseEntity<PunchOutSessionDTO> getSessionByKey(@PathVariable String sessionKey) {
        log.info("GET /api/v1/sessions/{} - Fetching session details", sessionKey);
        
        PunchOutSessionDTO session = sessionService.getSessionByKey(sessionKey);
        
        return ResponseEntity.ok(session);
    }
}
