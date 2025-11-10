package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.CatalogRouteDTO;
import com.waters.punchout.mongo.entity.CatalogRouteDocument;
import com.waters.punchout.mongo.repository.CatalogRouteMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogRouteMongoService {
    
    private final CatalogRouteMongoRepository repository;
    
    public List<CatalogRouteDTO> getAllRoutes() {
        log.info("Fetching all catalog routes");
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CatalogRouteDTO> getActiveRoutes() {
        log.info("Fetching active catalog routes");
        return repository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CatalogRouteDTO getRouteById(String id) {
        log.info("Fetching catalog route by id: {}", id);
        CatalogRouteDocument document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catalog route not found: " + id));
        return convertToDTO(document);
    }
    
    public List<CatalogRouteDTO> getRoutesByNetwork(String network) {
        log.info("Fetching catalog routes by network: {}", network);
        return repository.findByNetwork(network).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private CatalogRouteDTO convertToDTO(CatalogRouteDocument document) {
        CatalogRouteDTO dto = new CatalogRouteDTO();
        dto.setId(document.getId());
        dto.setRouteName(document.getRouteName());
        dto.setDomain(document.getDomain());
        dto.setNetwork(document.getNetwork());
        dto.setType(document.getType());
        dto.setDescription(document.getDescription());
        dto.setActive(document.getActive());
        dto.setCreatedDate(document.getCreatedDate());
        dto.setLastModified(document.getLastModified());
        
        if (document.getEnvironments() != null) {
            List<CatalogRouteDTO.EnvironmentConfigDTO> envDTOs = document.getEnvironments().stream()
                    .map(env -> {
                        CatalogRouteDTO.EnvironmentConfigDTO envDTO = new CatalogRouteDTO.EnvironmentConfigDTO();
                        envDTO.setEnvironment(env.getEnvironment());
                        envDTO.setUrl(env.getUrl());
                        envDTO.setUsername(env.getUsername());
                        envDTO.setPassword(env.getPassword());
                        envDTO.setSharedSecret(env.getSharedSecret());
                        envDTO.setEnabled(env.getEnabled());
                        envDTO.setNotes(env.getNotes());
                        return envDTO;
                    })
                    .collect(Collectors.toList());
            dto.setEnvironments(envDTOs);
        }
        
        return dto;
    }
}
