package com.waters.punchout.service;

import com.waters.punchout.dto.GatewayRequestDTO;
import com.waters.punchout.entity.GatewayRequest;
import com.waters.punchout.exception.InvalidDataException;
import com.waters.punchout.mapper.GatewayRequestMapper;
import com.waters.punchout.repository.GatewayRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class GatewayRequestService {
    
    private final GatewayRequestRepository gatewayRequestRepository;
    private final GatewayRequestMapper gatewayRequestMapper;
    
    @Transactional
    public GatewayRequestDTO createGatewayRequest(GatewayRequestDTO gatewayRequestDTO) {
        log.info("Creating gateway request for session key: {} with URI: {}", 
            gatewayRequestDTO.getSessionKey(), gatewayRequestDTO.getUri());
        
        if (gatewayRequestDTO.getSessionKey() == null || gatewayRequestDTO.getSessionKey().trim().isEmpty()) {
            throw new InvalidDataException("Session key cannot be null or empty");
        }
        
        if (gatewayRequestDTO.getUri() == null || gatewayRequestDTO.getUri().trim().isEmpty()) {
            throw new InvalidDataException("URI cannot be null or empty");
        }
        
        GatewayRequest gatewayRequest = gatewayRequestMapper.toEntity(gatewayRequestDTO);
        if (gatewayRequest.getDatetime() == null) {
            gatewayRequest.setDatetime(LocalDateTime.now());
        }
        
        GatewayRequest savedRequest = gatewayRequestRepository.save(gatewayRequest);
        
        log.info("Successfully created gateway request with ID: {} for session key: {}", 
            savedRequest.getId(), savedRequest.getSessionKey());
        
        return gatewayRequestMapper.toDTO(savedRequest);
    }
    
    @Transactional(readOnly = true)
    public List<GatewayRequestDTO> getGatewayRequestsBySessionKey(String sessionKey) {
        log.info("Fetching gateway requests for session key: {}", sessionKey);
        
        List<GatewayRequest> requests = gatewayRequestRepository.findBySessionKeyOrderByDatetimeDesc(sessionKey);
        
        return requests.stream()
            .map(gatewayRequestMapper::toDTO)
            .collect(Collectors.toList());
    }
}
