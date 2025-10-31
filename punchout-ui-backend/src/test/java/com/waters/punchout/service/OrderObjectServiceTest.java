package com.waters.punchout.service;

import com.waters.punchout.dto.OrderObjectDTO;
import com.waters.punchout.entity.OrderObject;
import com.waters.punchout.exception.InvalidDataException;
import com.waters.punchout.mapper.OrderObjectMapper;
import com.waters.punchout.repository.OrderObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderObjectServiceTest {
    
    @Mock
    private OrderObjectRepository orderObjectRepository;
    
    @Mock
    private OrderObjectMapper orderObjectMapper;
    
    @InjectMocks
    private OrderObjectService orderObjectService;
    
    private OrderObjectDTO orderObjectDTO;
    private OrderObject orderObjectEntity;
    
    @BeforeEach
    void setUp() {
        orderObjectDTO = new OrderObjectDTO();
        orderObjectDTO.setSessionKey("TEST-SESSION-123");
        orderObjectDTO.setType("PURCHASE_ORDER");
        orderObjectDTO.setOperation("CREATE");
        orderObjectDTO.setUserEmail("user@example.com");
        orderObjectDTO.setCompanyCode("COMP001");
        
        orderObjectEntity = new OrderObject();
        orderObjectEntity.setSessionKey("TEST-SESSION-123");
        orderObjectEntity.setType("PURCHASE_ORDER");
        orderObjectEntity.setOperation("CREATE");
        orderObjectEntity.setUserEmail("user@example.com");
        orderObjectEntity.setCompanyCode("COMP001");
    }
    
    @Test
    void testCreateOrderObject_Success() {
        when(orderObjectMapper.toEntity(any(OrderObjectDTO.class))).thenReturn(orderObjectEntity);
        when(orderObjectRepository.save(any(OrderObject.class))).thenReturn(orderObjectEntity);
        when(orderObjectMapper.toDTO(any(OrderObject.class))).thenReturn(orderObjectDTO);
        
        OrderObjectDTO result = orderObjectService.createOrderObject(orderObjectDTO);
        
        assertNotNull(result);
        assertEquals("TEST-SESSION-123", result.getSessionKey());
        assertEquals("PURCHASE_ORDER", result.getType());
        
        verify(orderObjectRepository, times(1)).save(any(OrderObject.class));
    }
    
    @Test
    void testCreateOrderObject_NullSessionKey_ThrowsException() {
        orderObjectDTO.setSessionKey(null);
        
        assertThrows(InvalidDataException.class, () -> orderObjectService.createOrderObject(orderObjectDTO));
        verify(orderObjectRepository, never()).save(any(OrderObject.class));
    }
    
    @Test
    void testGetOrderObjectBySessionKey_Success() {
        when(orderObjectRepository.findBySessionKey("TEST-SESSION-123")).thenReturn(Optional.of(orderObjectEntity));
        when(orderObjectMapper.toDTO(any(OrderObject.class))).thenReturn(orderObjectDTO);
        
        OrderObjectDTO result = orderObjectService.getOrderObjectBySessionKey("TEST-SESSION-123");
        
        assertNotNull(result);
        assertEquals("TEST-SESSION-123", result.getSessionKey());
        
        verify(orderObjectRepository, times(1)).findBySessionKey("TEST-SESSION-123");
    }
    
    @Test
    void testGetOrderObjectBySessionKey_NotFound_ReturnsNull() {
        when(orderObjectRepository.findBySessionKey("INVALID-KEY")).thenReturn(Optional.empty());
        
        OrderObjectDTO result = orderObjectService.getOrderObjectBySessionKey("INVALID-KEY");
        
        assertNull(result);
    }
}
