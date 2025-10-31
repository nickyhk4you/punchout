package com.waters.punchout.controller;

import com.waters.punchout.dto.OrderObjectDTO;
import com.waters.punchout.service.OrderObjectService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/punchout-sessions/{sessionKey}/order-object")
@RequiredArgsConstructor
@Slf4j
public class OrderObjectController {
    
    private final OrderObjectService orderObjectService;
    
    @GetMapping
    public ResponseEntity<OrderObjectDTO> getOrderObject(@PathVariable String sessionKey) {
        log.info("GET /api/punchout-sessions/{}/order-object - Fetching order object", sessionKey);
        
        OrderObjectDTO orderObject = orderObjectService.getOrderObjectBySessionKey(sessionKey);
        if (orderObject == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderObject);
    }
    
    @PostMapping
    public ResponseEntity<OrderObjectDTO> createOrUpdateOrderObject(
            @PathVariable String sessionKey,
            @Valid @RequestBody OrderObjectDTO orderObjectDTO) {
        log.info("POST /api/punchout-sessions/{}/order-object - Creating/updating order object", sessionKey);
        
        orderObjectDTO.setSessionKey(sessionKey);
        OrderObjectDTO savedOrderObject = orderObjectService.createOrderObject(orderObjectDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrderObject);
    }
}
