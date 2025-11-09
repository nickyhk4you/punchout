package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.mongo.entity.PunchOutSessionDocument;
import com.waters.punchout.mongo.repository.PunchOutSessionMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PunchOutSessionMongoService {
    
    private final PunchOutSessionMongoRepository mongoRepository;
    
    public List<PunchOutSessionDTO> getAllSessions() {
        log.info("Fetching all sessions from MongoDB");
        
        return mongoRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public PunchOutSessionDTO getSessionByKey(String sessionKey) {
        log.info("Fetching session with key: {} from MongoDB", sessionKey);
        
        PunchOutSessionDocument document = mongoRepository.findBySessionKey(sessionKey)
            .orElseThrow(() -> new RuntimeException("Session not found with key: " + sessionKey));
        
        return toDTO(document);
    }
    
    private PunchOutSessionDTO toDTO(PunchOutSessionDocument document) {
        PunchOutSessionDTO dto = new PunchOutSessionDTO();
        dto.setSessionKey(document.getSessionKey());
        dto.setCartReturn(document.getCartReturn());
        dto.setOperation(document.getOperation());
        dto.setContact(document.getContact());
        dto.setRouteName(document.getRouteName());
        dto.setEnvironment(document.getEnvironment());
        dto.setFlags(document.getFlags());
        dto.setSessionDate(document.getSessionDate());
        dto.setPunchedIn(document.getPunchedIn());
        dto.setPunchedOut(document.getPunchedOut());
        dto.setOrderId(document.getOrderId());
        dto.setOrderValue(document.getOrderValue());
        dto.setLineItems(document.getLineItems());
        dto.setItemQuantity(document.getItemQuantity());
        dto.setCatalog(document.getCatalog());
        dto.setNetwork(document.getNetwork());
        dto.setParser(document.getParser());
        dto.setBuyerCookie(document.getBuyerCookie());
        return dto;
    }
}
