package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.PunchOutTestDTO;
import com.waters.punchout.mongo.entity.PunchOutTestDocument;
import com.waters.punchout.mongo.repository.PunchOutTestMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PunchOutTestMongoService {
    
    private final PunchOutTestMongoRepository repository;
    
    public List<PunchOutTestDTO> getAllTests() {
        log.info("Fetching all punchout tests");
        return repository.findAllByOrderByTestDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public PunchOutTestDTO getTestById(String id) {
        log.info("Fetching punchout test by id: {}", id);
        PunchOutTestDocument document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PunchOut test not found: " + id));
        return convertToDTO(document);
    }
    
    public List<PunchOutTestDTO> getTestsByCatalogRoute(String routeId) {
        log.info("Fetching punchout tests by catalog route id: {}", routeId);
        return repository.findByCatalogRouteIdOrderByTestDateDesc(routeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public PunchOutTestDTO createTest(PunchOutTestDTO dto) {
        log.info("Creating new punchout test: {}", dto.getTestName());
        PunchOutTestDocument document = convertToDocument(dto);
        PunchOutTestDocument savedDocument = repository.save(document);
        log.info("Created punchout test with id: {}", savedDocument.getId());
        return convertToDTO(savedDocument);
    }
    
    public PunchOutTestDTO updateTest(String id, PunchOutTestDTO dto) {
        log.info("Updating punchout test: {}", id);
        PunchOutTestDocument existingDocument = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PunchOut test not found: " + id));
        
        PunchOutTestDocument updatedDocument = convertToDocument(dto);
        updatedDocument.setId(existingDocument.getId());
        
        PunchOutTestDocument savedDocument = repository.save(updatedDocument);
        log.info("Updated punchout test: {}", id);
        return convertToDTO(savedDocument);
    }
    
    private PunchOutTestDTO convertToDTO(PunchOutTestDocument document) {
        PunchOutTestDTO dto = new PunchOutTestDTO();
        dto.setId(document.getId());
        dto.setTestName(document.getTestName());
        dto.setCatalogRouteId(document.getCatalogRouteId());
        dto.setCatalogRouteName(document.getCatalogRouteName());
        dto.setEnvironment(document.getEnvironment());
        dto.setTester(document.getTester());
        dto.setTestDate(document.getTestDate());
        dto.setStatus(document.getStatus());
        dto.setSetupRequestSent(document.getSetupRequestSent());
        dto.setSetupResponseReceived(document.getSetupResponseReceived());
        dto.setCatalogUrl(document.getCatalogUrl());
        dto.setOrderMessageSent(document.getOrderMessageSent());
        dto.setOrderMessageReceived(document.getOrderMessageReceived());
        dto.setTotalDuration(document.getTotalDuration());
        dto.setSetupRequest(document.getSetupRequest());
        dto.setSetupResponse(document.getSetupResponse());
        dto.setOrderMessage(document.getOrderMessage());
        dto.setOrderResponse(document.getOrderResponse());
        dto.setErrorMessage(document.getErrorMessage());
        dto.setNotes(document.getNotes());
        return dto;
    }
    
    private PunchOutTestDocument convertToDocument(PunchOutTestDTO dto) {
        PunchOutTestDocument document = new PunchOutTestDocument();
        document.setId(dto.getId());
        document.setTestName(dto.getTestName());
        document.setCatalogRouteId(dto.getCatalogRouteId());
        document.setCatalogRouteName(dto.getCatalogRouteName());
        document.setEnvironment(dto.getEnvironment());
        document.setTester(dto.getTester());
        document.setTestDate(dto.getTestDate());
        document.setStatus(dto.getStatus());
        document.setSetupRequestSent(dto.getSetupRequestSent());
        document.setSetupResponseReceived(dto.getSetupResponseReceived());
        document.setCatalogUrl(dto.getCatalogUrl());
        document.setOrderMessageSent(dto.getOrderMessageSent());
        document.setOrderMessageReceived(dto.getOrderMessageReceived());
        document.setTotalDuration(dto.getTotalDuration());
        document.setSetupRequest(dto.getSetupRequest());
        document.setSetupResponse(dto.getSetupResponse());
        document.setOrderMessage(dto.getOrderMessage());
        document.setOrderResponse(dto.getOrderResponse());
        document.setErrorMessage(dto.getErrorMessage());
        document.setNotes(dto.getNotes());
        return document;
    }
}
