package com.waters.punchout.controller;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.service.PunchOutSessionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/punchout-sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Profile("local")
public class PunchOutSessionController {
    
    private final PunchOutSessionService sessionService;
    
    @GetMapping
    public ResponseEntity<List<PunchOutSessionDTO>> getAllSessions(
            @RequestParam(required = false) Map<String, String> filters) {
        log.info("GET /api/punchout-sessions - Fetching sessions with filters: {}", filters);
        
        List<PunchOutSessionDTO> sessions;
        if (filters != null && !filters.isEmpty()) {
            sessions = sessionService.getSessionsByFilter(filters);
        } else {
            sessions = sessionService.getAllSessions();
        }
        
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/{sessionKey}")
    public ResponseEntity<PunchOutSessionDTO> getSessionByKey(@PathVariable String sessionKey) {
        log.info("GET /api/punchout-sessions/{} - Fetching session", sessionKey);
        
        PunchOutSessionDTO session = sessionService.getSessionByKey(sessionKey);
        return ResponseEntity.ok(session);
    }
    
    @PostMapping
    public ResponseEntity<PunchOutSessionDTO> createSession(@Valid @RequestBody PunchOutSessionDTO sessionDTO) {
        log.info("POST /api/punchout-sessions - Creating new session");
        
        PunchOutSessionDTO createdSession = sessionService.createSession(sessionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }
    
    @PutMapping("/{sessionKey}")
    public ResponseEntity<PunchOutSessionDTO> updateSession(
            @PathVariable String sessionKey,
            @Valid @RequestBody PunchOutSessionDTO sessionDTO) {
        log.info("PUT /api/punchout-sessions/{} - Updating session", sessionKey);
        
        sessionDTO.setSessionKey(sessionKey);
        PunchOutSessionDTO updatedSession = sessionService.updateSession(sessionDTO);
        return ResponseEntity.ok(updatedSession);
    }
}
