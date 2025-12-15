package com.waters.punchout.mapper;

import com.waters.punchout.dto.GatewayRequestDTO;
import com.waters.punchout.entity.GatewayRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class GatewayRequestMapper {
    
    public GatewayRequestDTO toDTO(GatewayRequest entity) {
        if (entity == null) {
            return null;
        }
        
        GatewayRequestDTO dto = new GatewayRequestDTO();
        dto.setId(entity.getId());
        dto.setSessionKey(entity.getSessionKey());
        dto.setDatetime(entity.getDatetime());
        dto.setUri(entity.getUri());
        dto.setOpenLink(entity.getOpenLink());
        
        return dto;
    }
    
    public GatewayRequest toEntity(GatewayRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        GatewayRequest entity = new GatewayRequest();
        entity.setId(dto.getId());
        entity.setSessionKey(dto.getSessionKey());
        entity.setDatetime(dto.getDatetime());
        entity.setUri(dto.getUri());
        entity.setOpenLink(dto.getOpenLink());
        
        return entity;
    }
}
