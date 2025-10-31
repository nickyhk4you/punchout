package com.waters.punchout.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.service.PunchOutSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PunchOutSessionController.class)
@ActiveProfiles("test")
class PunchOutSessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PunchOutSessionService sessionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private PunchOutSessionDTO sessionDTO;
    
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
    }
    
    @Test
    void testGetAllSessions() throws Exception {
        List<PunchOutSessionDTO> sessions = Arrays.asList(sessionDTO);
        when(sessionService.getAllSessions()).thenReturn(sessions);
        
        mockMvc.perform(get("/api/punchout-sessions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].sessionKey").value("TEST-SESSION-123"))
            .andExpect(jsonPath("$[0].operation").value("CREATE"));
    }
    
    @Test
    void testGetSessionByKey() throws Exception {
        when(sessionService.getSessionByKey(anyString())).thenReturn(sessionDTO);
        
        mockMvc.perform(get("/api/punchout-sessions/TEST-SESSION-123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sessionKey").value("TEST-SESSION-123"))
            .andExpect(jsonPath("$.operation").value("CREATE"));
    }
    
    @Test
    void testCreateSession() throws Exception {
        when(sessionService.createSession(any(PunchOutSessionDTO.class))).thenReturn(sessionDTO);
        
        mockMvc.perform(post("/api/punchout-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sessionKey").value("TEST-SESSION-123"))
            .andExpect(jsonPath("$.operation").value("CREATE"));
    }
    
    @Test
    void testUpdateSession() throws Exception {
        when(sessionService.updateSession(any(PunchOutSessionDTO.class))).thenReturn(sessionDTO);
        
        mockMvc.perform(put("/api/punchout-sessions/TEST-SESSION-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionKey").value("TEST-SESSION-123"))
            .andExpect(jsonPath("$.operation").value("CREATE"));
    }
}
