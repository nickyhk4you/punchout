package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.NetworkRequestDTO;
import com.waters.punchout.mongo.entity.NetworkRequestDocument;
import com.waters.punchout.mongo.repository.NetworkRequestMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkRequestMongoService {
    
    private final NetworkRequestMongoRepository repository;
    
    public List<NetworkRequestDTO> getNetworkRequestsBySessionKey(String sessionKey) {
        log.info("Fetching network requests for session: {}", sessionKey);
        return repository.findBySessionKeyOrderByTimestampAsc(sessionKey).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<NetworkRequestDTO> getNetworkRequestsBySessionKeyAndDirection(String sessionKey, String direction) {
        log.info("Fetching {} network requests for session: {}", direction, sessionKey);
        return repository.findBySessionKeyAndDirection(sessionKey, direction).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public NetworkRequestDTO getNetworkRequestById(String id) {
        log.info("Fetching network request by id: {}", id);
        NetworkRequestDocument document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Network request not found: " + id));
        return convertToDTO(document);
    }
    
    private NetworkRequestDTO convertToDTO(NetworkRequestDocument document) {
        NetworkRequestDTO dto = new NetworkRequestDTO();
        dto.setId(document.getId());
        dto.setSessionKey(document.getSessionKey());
        dto.setRequestId(document.getRequestId());
        dto.setTimestamp(document.getTimestamp());
        dto.setDirection(document.getDirection());
        dto.setSource(document.getSource());
        dto.setDestination(document.getDestination());
        dto.setMethod(document.getMethod());
        dto.setUrl(document.getUrl());
        dto.setHeaders(document.getHeaders());
        dto.setRequestBody(document.getRequestBody());
        dto.setStatusCode(document.getStatusCode());
        dto.setResponseHeaders(document.getResponseHeaders());
        dto.setResponseBody(document.getResponseBody());
        dto.setDuration(document.getDuration());
        dto.setRequestType(document.getRequestType());
        dto.setSuccess(document.getSuccess());
        dto.setErrorMessage(document.getErrorMessage());
        return dto;
    }
}
