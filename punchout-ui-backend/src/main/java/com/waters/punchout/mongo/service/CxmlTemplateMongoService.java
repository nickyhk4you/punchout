package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.CxmlTemplateDTO;
import com.waters.punchout.mongo.entity.CxmlTemplateDocument;
import com.waters.punchout.mongo.repository.CxmlTemplateMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CxmlTemplateMongoService {
    
    private final CxmlTemplateMongoRepository repository;
    
    public List<CxmlTemplateDTO> getAllTemplates() {
        log.info("Fetching all cXML templates from MongoDB");
        return repository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<CxmlTemplateDTO> getTemplatesByEnvironment(String environment) {
        log.info("Fetching cXML templates for environment: {}", environment);
        return repository.findByEnvironment(environment).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public CxmlTemplateDTO getTemplateByEnvironmentAndCustomer(String environment, String customerId) {
        log.info("Fetching cXML template for environment: {}, customerId: {}", environment, customerId);
        return repository.findByEnvironmentAndCustomerId(environment, customerId)
            .map(this::toDTO)
            .orElse(null);
    }
    
    public CxmlTemplateDTO getDefaultTemplateForEnvironment(String environment) {
        log.info("Fetching default cXML template for environment: {}", environment);
        return repository.findByEnvironmentAndIsDefaultTrue(environment)
            .map(this::toDTO)
            .orElse(null);
    }
    
    public CxmlTemplateDTO saveTemplate(CxmlTemplateDTO dto) {
        log.info("Saving cXML template: {}", dto.getTemplateName());
        
        CxmlTemplateDocument document = new CxmlTemplateDocument();
        if (dto.getId() != null) {
            document.setId(dto.getId());
        }
        document.setTemplateName(dto.getTemplateName());
        document.setEnvironment(dto.getEnvironment());
        document.setCustomerId(dto.getCustomerId());
        document.setCustomerName(dto.getCustomerName());
        document.setCxmlTemplate(dto.getCxmlTemplate());
        document.setDescription(dto.getDescription());
        document.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        document.setCreatedBy(dto.getCreatedBy());
        
        if (dto.getId() == null) {
            document.setCreatedAt(LocalDateTime.now());
        }
        document.setUpdatedAt(LocalDateTime.now());
        
        CxmlTemplateDocument saved = repository.save(document);
        return toDTO(saved);
    }
    
    private CxmlTemplateDTO toDTO(CxmlTemplateDocument document) {
        CxmlTemplateDTO dto = new CxmlTemplateDTO();
        dto.setId(document.getId());
        dto.setTemplateName(document.getTemplateName());
        dto.setEnvironment(document.getEnvironment());
        dto.setCustomerId(document.getCustomerId());
        dto.setCustomerName(document.getCustomerName());
        dto.setCxmlTemplate(document.getCxmlTemplate());
        dto.setDescription(document.getDescription());
        dto.setIsDefault(document.getIsDefault());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setCreatedBy(document.getCreatedBy());
        return dto;
    }
}
