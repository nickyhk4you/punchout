package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.OrderDTO;
import com.waters.punchout.mongo.entity.OrderDocument;
import com.waters.punchout.mongo.repository.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderMongoService {
    
    private final OrderMongoRepository orderRepository;
    
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders from MongoDB");
        List<OrderDocument> orders = orderRepository.findAll();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public OrderDTO getOrderByOrderId(String orderId) {
        log.info("Fetching order by orderId: {}", orderId);
        return orderRepository.findByOrderId(orderId)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public List<OrderDTO> getOrdersByStatus(String status) {
        log.info("Fetching orders by status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        log.info("Fetching orders by customerId: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getOrdersByEnvironment(String environment) {
        log.info("Fetching orders by environment: {}", environment);
        return orderRepository.findByEnvironment(environment).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> getOrderStats() {
        log.info("Calculating order statistics");
        
        List<OrderDocument> allOrders = orderRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());
        
        BigDecimal totalValue = allOrders.stream()
                .map(OrderDocument::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalValue", totalValue);
        
        Map<String, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() != null ? order.getStatus() : "UNKNOWN",
                        Collectors.counting()
                ));
        stats.put("ordersByStatus", ordersByStatus);
        
        Map<String, Long> ordersByCustomer = allOrders.stream()
                .filter(order -> order.getCustomerId() != null)
                .collect(Collectors.groupingBy(
                        OrderDocument::getCustomerId,
                        Collectors.counting()
                ));
        stats.put("ordersByCustomer", ordersByCustomer);
        
        Map<String, Long> ordersByEnvironment = allOrders.stream()
                .filter(order -> order.getEnvironment() != null)
                .collect(Collectors.groupingBy(
                        OrderDocument::getEnvironment,
                        Collectors.counting()
                ));
        stats.put("ordersByEnvironment", ordersByEnvironment);
        
        return stats;
    }
    
    private OrderDTO convertToDTO(OrderDocument doc) {
        OrderDTO dto = new OrderDTO();
        dto.setId(doc.getId());
        dto.setOrderId(doc.getOrderId());
        dto.setSessionKey(doc.getSessionKey());
        dto.setOrderDate(doc.getOrderDate());
        dto.setOrderType(doc.getOrderType());
        dto.setOrderVersion(doc.getOrderVersion());
        dto.setCustomerId(doc.getCustomerId());
        dto.setCustomerName(doc.getCustomerName());
        dto.setTotal(doc.getTotal());
        dto.setCurrency(doc.getCurrency());
        dto.setTaxAmount(doc.getTaxAmount());
        dto.setShipTo(doc.getShipTo());
        dto.setBillTo(doc.getBillTo());
        dto.setItems(doc.getItems());
        dto.setExtrinsics(doc.getExtrinsics());
        dto.setComments(doc.getComments());
        dto.setStatus(doc.getStatus());
        dto.setReceivedAt(doc.getReceivedAt());
        dto.setProcessedAt(doc.getProcessedAt());
        dto.setMuleOrderId(doc.getMuleOrderId());
        dto.setEnvironment(doc.getEnvironment());
        dto.setSource(doc.getSource());
        dto.setDialect(doc.getDialect());
        return dto;
    }
}
