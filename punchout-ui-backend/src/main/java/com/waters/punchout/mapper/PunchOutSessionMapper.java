package com.waters.punchout.mapper;

import com.waters.punchout.dto.PunchOutSessionDTO;
import com.waters.punchout.entity.PunchOutSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class PunchOutSessionMapper {
    
    public PunchOutSessionDTO toDTO(PunchOutSession entity) {
        if (entity == null) {
            return null;
        }
        
        PunchOutSessionDTO dto = new PunchOutSessionDTO();
        dto.setSessionKey(entity.getSessionKey());
        dto.setCartReturn(entity.getCartReturn());
        dto.setOperation(entity.getOperation());
        dto.setContactEmail(entity.getContactEmail());
        dto.setRouteName(entity.getRouteName());
        dto.setEnvironment(entity.getEnvironment());
        dto.setFlags(entity.getFlags());
        dto.setSessionDate(entity.getSessionDate());
        dto.setPunchedIn(entity.getPunchedIn());
        dto.setPunchedOut(entity.getPunchedOut());
        dto.setOrderId(entity.getOrderId());
        dto.setOrderValue(entity.getOrderValue());
        dto.setLineItems(entity.getLineItems());
        dto.setItemQuantity(entity.getItemQuantity());
        dto.setCatalog(entity.getCatalog());
        dto.setNetwork(entity.getNetwork());
        dto.setParser(entity.getParser());
        dto.setBuyerCookie(entity.getBuyerCookie());
        
        return dto;
    }
    
    public PunchOutSession toEntity(PunchOutSessionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        PunchOutSession entity = new PunchOutSession();
        entity.setSessionKey(dto.getSessionKey());
        entity.setCartReturn(dto.getCartReturn());
        entity.setOperation(dto.getOperation());
        entity.setContactEmail(dto.getContactEmail());
        entity.setRouteName(dto.getRouteName());
        entity.setEnvironment(dto.getEnvironment());
        entity.setFlags(dto.getFlags());
        entity.setSessionDate(dto.getSessionDate());
        entity.setPunchedIn(dto.getPunchedIn());
        entity.setPunchedOut(dto.getPunchedOut());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderValue(dto.getOrderValue());
        entity.setLineItems(dto.getLineItems());
        entity.setItemQuantity(dto.getItemQuantity());
        entity.setCatalog(dto.getCatalog());
        entity.setNetwork(dto.getNetwork());
        entity.setParser(dto.getParser());
        entity.setBuyerCookie(dto.getBuyerCookie());
        
        return entity;
    }
}
