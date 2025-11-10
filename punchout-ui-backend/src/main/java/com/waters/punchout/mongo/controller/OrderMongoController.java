package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.NetworkRequestDTO;
import com.waters.punchout.dto.OrderDTO;
import com.waters.punchout.mongo.service.NetworkRequestMongoService;
import com.waters.punchout.mongo.service.OrderMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderMongoController {
    
    private final OrderMongoService orderService;
    private final NetworkRequestMongoService networkRequestService;
    
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String environment
    ) {
        log.info("GET /api/v1/orders - status={}, customerId={}, environment={}", status, customerId, environment);
        
        List<OrderDTO> orders;
        
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
        } else if (customerId != null && !customerId.isEmpty()) {
            orders = orderService.getOrdersByCustomerId(customerId);
        } else if (environment != null && !environment.isEmpty()) {
            orders = orderService.getOrdersByEnvironment(environment);
        } else {
            orders = orderService.getAllOrders();
        }
        
        log.info("Returning {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByOrderId(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{}", orderId);
        
        OrderDTO order = orderService.getOrderByOrderId(orderId);
        
        if (order == null) {
            log.warn("Order not found: {}", orderId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/{orderId}/network-requests")
    public ResponseEntity<List<NetworkRequestDTO>> getNetworkRequestsForOrder(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{}/network-requests", orderId);
        
        List<NetworkRequestDTO> allRequests = networkRequestService.getAllNetworkRequests();
        
        List<NetworkRequestDTO> orderRequests = allRequests.stream()
                .filter(req -> orderId.equals(req.getOrderId()))
                .collect(Collectors.toList());
        
        log.info("Found {} network requests for order {}", orderRequests.size(), orderId);
        return ResponseEntity.ok(orderRequests);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        log.info("GET /api/v1/orders/stats");
        
        Map<String, Object> stats = orderService.getOrderStats();
        
        return ResponseEntity.ok(stats);
    }
}
