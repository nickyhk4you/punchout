package com.waters.punchout.service;

import com.waters.punchout.dto.OrderObjectDTO;
import com.waters.punchout.entity.OrderObject;
import com.waters.punchout.exception.InvalidDataException;
import com.waters.punchout.mapper.OrderObjectMapper;
import com.waters.punchout.repository.OrderObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderObjectService {
    
    private final OrderObjectRepository orderObjectRepository;
    private final OrderObjectMapper orderObjectMapper;
    
    @Transactional
    public OrderObjectDTO createOrderObject(OrderObjectDTO orderObjectDTO) {
        log.info("Creating order object for session key: {}", orderObjectDTO.getSessionKey());
        
        if (orderObjectDTO.getSessionKey() == null || orderObjectDTO.getSessionKey().trim().isEmpty()) {
            throw new InvalidDataException("Session key cannot be null or empty");
        }
        
        OrderObject orderObject = orderObjectMapper.toEntity(orderObjectDTO);
        OrderObject savedOrderObject = orderObjectRepository.save(orderObject);
        
        log.info("Successfully created order object for session key: {}", savedOrderObject.getSessionKey());
        
        return orderObjectMapper.toDTO(savedOrderObject);
    }
    
    @Transactional(readOnly = true)
    public OrderObjectDTO getOrderObjectBySessionKey(String sessionKey) {
        log.info("Fetching order object for session key: {}", sessionKey);
        
        return orderObjectRepository.findBySessionKey(sessionKey)
            .map(orderObjectMapper::toDTO)
            .orElse(null);
    }
}
