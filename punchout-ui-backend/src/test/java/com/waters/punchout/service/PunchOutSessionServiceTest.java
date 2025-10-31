package com.waters.punchout.service;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.entity.PunchOutSession;
import com.waters.punchout.exception.InvalidDataException;
import com.waters.punchout.exception.SessionNotFoundException;
import com.waters.punchout.mapper.PunchOutSessionMapper;
import com.waters.punchout.repository.PunchOutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PunchOutSessionServiceTest {
    
    @Mock
    private PunchOutSessionRepository sessionRepository;
    
    @Mock
    private PunchOutSessionMapper sessionMapper;
    
    @InjectMocks
    private PunchOutSessionService sessionService;
    
    private PunchOutSessionDTO sessionDTO;
    private PunchOutSession sessionEntity;
    
    @BeforeEach
    void setUp() {
        sessionDTO = new PunchOutSessionDTO();
        sessionDTO.setSessionKey("TEST-SESSION-123");
        sessionDTO.setOperation("CREATE");
        sessionDTO.setContactEmail("test@example.com");
        sessionDTO.setRouteName("testRoute");
        sessionDTO.setEnvironment("DEV");
        sessionDTO.setSessionDate(LocalDateTime.now());
        sessionDTO.setOrderValue(new BigDecimal("1000.00"));
        
        sessionEntity = new PunchOutSession();
        sessionEntity.setSessionKey("TEST-SESSION-123");
        sessionEntity.setOperation("CREATE");
        sessionEntity.setContactEmail("test@example.com");
        sessionEntity.setRouteName("testRoute");
        sessionEntity.setEnvironment("DEV");
        sessionEntity.setSessionDate(LocalDateTime.now());
        sessionEntity.setOrderValue(new BigDecimal("1000.00"));
    }
    
    @Test
    void testCreateSession_Success() {
        when(sessionMapper.toEntity(any(PunchOutSessionDTO.class))).thenReturn(sessionEntity);
        when(sessionRepository.save(any(PunchOutSession.class))).thenReturn(sessionEntity);
        when(sessionMapper.toDTO(any(PunchOutSession.class))).thenReturn(sessionDTO);
        
        PunchOutSessionDTO result = sessionService.createSession(sessionDTO);
        
        assertNotNull(result);
        assertEquals("TEST-SESSION-123", result.getSessionKey());
        assertEquals("CREATE", result.getOperation());
        
        verify(sessionRepository, times(1)).save(any(PunchOutSession.class));
    }
    
    @Test
    void testCreateSession_NullSessionKey_ThrowsException() {
        sessionDTO.setSessionKey(null);
        
        assertThrows(InvalidDataException.class, () -> sessionService.createSession(sessionDTO));
        verify(sessionRepository, never()).save(any(PunchOutSession.class));
    }
    
    @Test
    void testCreateSession_EmptyOperation_ThrowsException() {
        sessionDTO.setOperation("");
        
        assertThrows(InvalidDataException.class, () -> sessionService.createSession(sessionDTO));
        verify(sessionRepository, never()).save(any(PunchOutSession.class));
    }
    
    @Test
    void testGetSessionByKey_Success() {
        when(sessionRepository.findBySessionKey("TEST-SESSION-123")).thenReturn(Optional.of(sessionEntity));
        when(sessionMapper.toDTO(any(PunchOutSession.class))).thenReturn(sessionDTO);
        
        PunchOutSessionDTO result = sessionService.getSessionByKey("TEST-SESSION-123");
        
        assertNotNull(result);
        assertEquals("TEST-SESSION-123", result.getSessionKey());
        
        verify(sessionRepository, times(1)).findBySessionKey("TEST-SESSION-123");
    }
    
    @Test
    void testGetSessionByKey_NotFound_ThrowsException() {
        when(sessionRepository.findBySessionKey("INVALID-KEY")).thenReturn(Optional.empty());
        
        assertThrows(SessionNotFoundException.class, () -> sessionService.getSessionByKey("INVALID-KEY"));
    }
    
    @Test
    void testUpdateSession_Success() {
        when(sessionRepository.findBySessionKey("TEST-SESSION-123")).thenReturn(Optional.of(sessionEntity));
        when(sessionMapper.toEntity(any(PunchOutSessionDTO.class))).thenReturn(sessionEntity);
        when(sessionRepository.save(any(PunchOutSession.class))).thenReturn(sessionEntity);
        when(sessionMapper.toDTO(any(PunchOutSession.class))).thenReturn(sessionDTO);
        
        PunchOutSessionDTO result = sessionService.updateSession(sessionDTO);
        
        assertNotNull(result);
        assertEquals("TEST-SESSION-123", result.getSessionKey());
        
        verify(sessionRepository, times(1)).save(any(PunchOutSession.class));
    }
}
