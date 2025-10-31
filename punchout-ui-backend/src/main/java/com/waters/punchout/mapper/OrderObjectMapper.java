package com.waters.punchout.mapper;

import com.waters.punchout.dto.OrderObjectDTO;
import com.waters.punchout.entity.OrderObject;
import org.springframework.stereotype.Component;

@Component
public class OrderObjectMapper {
    
    public OrderObjectDTO toDTO(OrderObject entity) {
        if (entity == null) {
            return null;
        }
        
        OrderObjectDTO dto = new OrderObjectDTO();
        dto.setSessionKey(entity.getSessionKey());
        dto.setType(entity.getType());
        dto.setOperation(entity.getOperation());
        dto.setMode(entity.getMode());
        dto.setUniqueName(entity.getUniqueName());
        dto.setUserEmail(entity.getUserEmail());
        dto.setCompanyCode(entity.getCompanyCode());
        dto.setUserFirstName(entity.getUserFirstName());
        dto.setUserLastName(entity.getUserLastName());
        dto.setFromIdentity(entity.getFromIdentity());
        dto.setSoldToLookup(entity.getSoldToLookup());
        dto.setContactEmail(entity.getContactEmail());
        
        return dto;
    }
    
    public OrderObject toEntity(OrderObjectDTO dto) {
        if (dto == null) {
            return null;
        }
        
        OrderObject entity = new OrderObject();
        entity.setSessionKey(dto.getSessionKey());
        entity.setType(dto.getType());
        entity.setOperation(dto.getOperation());
        entity.setMode(dto.getMode());
        entity.setUniqueName(dto.getUniqueName());
        entity.setUserEmail(dto.getUserEmail());
        entity.setCompanyCode(dto.getCompanyCode());
        entity.setUserFirstName(dto.getUserFirstName());
        entity.setUserLastName(dto.getUserLastName());
        entity.setFromIdentity(dto.getFromIdentity());
        entity.setSoldToLookup(dto.getSoldToLookup());
        entity.setContactEmail(dto.getContactEmail());
        
        return entity;
    }
}
