package com.waters.punchout.service;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.entity.PunchOutSession;
import com.waters.punchout.exception.InvalidDataException;
import com.waters.punchout.exception.SessionNotFoundException;
import com.waters.punchout.mapper.PunchOutSessionMapper;
import com.waters.punchout.repository.PunchOutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class PunchOutSessionService {
    
    private final PunchOutSessionRepository sessionRepository;
    private final PunchOutSessionMapper sessionMapper;
    
    @Transactional
    public PunchOutSessionDTO createSession(PunchOutSessionDTO sessionDTO) {
        log.info("Creating PunchOut session with key: {}", sessionDTO.getSessionKey());
        
        if (sessionDTO.getSessionKey() == null || sessionDTO.getSessionKey().trim().isEmpty()) {
            throw new InvalidDataException("Session key cannot be null or empty");
        }
        
        if (sessionDTO.getOperation() == null || sessionDTO.getOperation().trim().isEmpty()) {
            throw new InvalidDataException("Operation cannot be null or empty");
        }
        
        PunchOutSession session = sessionMapper.toEntity(sessionDTO);
        if (session.getSessionDate() == null) {
            session.setSessionDate(LocalDateTime.now());
        }
        
        PunchOutSession savedSession = sessionRepository.save(session);
        log.info("Successfully created session with key: {}", savedSession.getSessionKey());
        
        return sessionMapper.toDTO(savedSession);
    }
    
    @Transactional(readOnly = true)
    public PunchOutSessionDTO getSessionByKey(String sessionKey) {
        log.info("Fetching session with key: {}", sessionKey);
        
        PunchOutSession session = sessionRepository.findBySessionKey(sessionKey)
            .orElseThrow(() -> new SessionNotFoundException("Session not found with key: " + sessionKey));
        
        return sessionMapper.toDTO(session);
    }
    
    @Transactional(readOnly = true)
    public List<PunchOutSessionDTO> getAllSessions() {
        log.info("Fetching all sessions");
        
        return sessionRepository.findAll().stream()
            .map(sessionMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PunchOutSessionDTO> getSessionsByFilter(Map<String, String> filterCriteria) {
        log.info("Fetching sessions with filters: {}", filterCriteria);
        
        String operation = filterCriteria.get("operation");
        String routeName = filterCriteria.get("routeName");
        String environment = filterCriteria.get("environment");
        
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (filterCriteria.containsKey("startDate")) {
            startDate = LocalDateTime.parse(filterCriteria.get("startDate"));
        }
        
        if (filterCriteria.containsKey("endDate")) {
            endDate = LocalDateTime.parse(filterCriteria.get("endDate"));
        }
        
        List<PunchOutSession> sessions = sessionRepository.findByFilters(
            operation, routeName, environment, startDate, endDate
        );
        
        return sessions.stream()
            .map(sessionMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PunchOutSessionDTO updateSession(PunchOutSessionDTO sessionDTO) {
        log.info("Updating session with key: {}", sessionDTO.getSessionKey());
        
        PunchOutSession existingSession = sessionRepository.findBySessionKey(sessionDTO.getSessionKey())
            .orElseThrow(() -> new SessionNotFoundException("Session not found with key: " + sessionDTO.getSessionKey()));
        
        PunchOutSession updatedSession = sessionMapper.toEntity(sessionDTO);
        PunchOutSession savedSession = sessionRepository.save(updatedSession);
        
        log.info("Successfully updated session with key: {}", savedSession.getSessionKey());
        
        return sessionMapper.toDTO(savedSession);
    }
}
