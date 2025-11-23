package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.client.AuthServiceClient;
import com.waters.punchout.gateway.client.MuleServiceClient;
import com.waters.punchout.gateway.converter.CxmlOrderConverter;
import com.waters.punchout.common.dto.OrderResponse;
import com.waters.punchout.gateway.entity.OrderDocument;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.metrics.MetricsService;
import com.waters.punchout.gateway.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderOrchestrationService {
    
    private final CxmlOrderConverter orderConverter;
    private final AuthServiceClient authServiceClient;
    private final MuleServiceClient muleServiceClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final OrderRepository orderRepository;
    private final MetricsService metricsService;
    
    public OrderResponse processOrder(String cxmlContent) {
        log.info("Processing order request");
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String environment = "dev";
        
        try {
            OrderDocument order = orderConverter.convertCxmlToOrder(cxmlContent);
            log.info("Parsed order: orderId={}, items={}, total={}", 
                    order.getOrderId(), order.getItems().size(), order.getTotal());
            
            environment = order.getEnvironment() != null ? order.getEnvironment() : "dev";
            
            // Generate deterministic order ID for idempotency
            String idempotentOrderId = generateIdempotentOrderId(order);
            
            // Check if order already exists
            Optional<OrderDocument> existingOrder = orderRepository.findByOrderId(idempotentOrderId);
            if (existingOrder.isPresent()) {
                log.info("Order already exists, returning existing order: orderId={}", idempotentOrderId);
                OrderDocument existing = existingOrder.get();
                success = true;
                long duration = System.currentTimeMillis() - startTime;
                metricsService.recordOrderProcessing(environment, duration, true);
                return new OrderResponse(existing.getOrderId(), existing.getMuleOrderId(), "success", 
                        "Order already processed (idempotent)");
            }
            
            order.setOrderId(idempotentOrderId);
            
            logInboundOrderRequest(cxmlContent, order.getOrderId(), order.getSessionKey());
            
            String token = getAuthToken(order);
            
            Map<String, Object> jsonOrder = orderConverter.convertOrderToJson(order);
            
            Map<String, Object> muleResponse = sendOrderToMule(jsonOrder, token, order.getOrderId());
            
            order.setMuleOrderId((String) muleResponse.get("muleOrderId"));
            order.setStatus("CONFIRMED");
            order.setProcessedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            success = true;
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordOrderProcessing(environment, duration, true);
            
            log.info("Order processed successfully: orderId={}, muleOrderId={}", 
                    order.getOrderId(), order.getMuleOrderId());
            
            return new OrderResponse(order.getOrderId(), order.getMuleOrderId(), "success", 
                    "Order processed successfully");
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordOrderProcessing(environment, duration, false);
            log.error("Failed to process order: {}", e.getMessage(), e);
            throw new RuntimeException("Order processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a deterministic order ID based on order content for idempotency.
     * Uses SHA-256 hash of sessionKey + orderDate + total + items.
     */
    private String generateIdempotentOrderId(OrderDocument order) {
        try {
            // Create a deterministic string from order data
            StringBuilder content = new StringBuilder();
            content.append(order.getSessionKey() != null ? order.getSessionKey() : "");
            content.append("|");
            content.append(order.getOrderDate() != null ? order.getOrderDate().toString() : "");
            content.append("|");
            content.append(order.getTotal() != null ? order.getTotal().toString() : "");
            content.append("|");
            content.append(order.getCustomerId() != null ? order.getCustomerId() : "");
            content.append("|");
            if (order.getItems() != null) {
                order.getItems().forEach(item -> {
                    content.append(item.getSupplierPartId()).append(":").append(item.getQuantity()).append(",");
                });
            }
            
            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.toString().getBytes(StandardCharsets.UTF_8));
            String hexHash = HexFormat.of().formatHex(hash);
            
            // Return first 16 characters for readability
            return "ORD_" + hexHash.substring(0, 16).toUpperCase();
            
        } catch (Exception e) {
            log.warn("Failed to generate idempotent order ID, using fallback: {}", e.getMessage());
            // Fallback to original orderId or timestamp-based ID
            return order.getOrderId() != null ? order.getOrderId() : "ORD_" + System.currentTimeMillis();
        }
    }
    
    private void logInboundOrderRequest(String cxmlContent, String orderId, String sessionKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        
        NetworkRequestLogger.OrderContext orderContext = new NetworkRequestLogger.OrderContext(orderId);
        
        networkRequestLogger.logInboundOrderRequest(
                sessionKey,
                orderId,
                "Customer System",
                "Punchout Gateway",
                "POST",
                "/punchout/order",
                headers,
                cxmlContent,
                "cXML-Order"
        );
    }
    
    private String getAuthToken(OrderDocument order) {
        try {
            log.debug("Obtaining auth token for order: {}", order.getOrderId());
            return "Bearer mock-token-" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to obtain auth token: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> sendOrderToMule(Map<String, Object> jsonOrder, String token, String orderId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Sending order to Mule: orderId={}", orderId);
            
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.put(HttpHeaders.AUTHORIZATION, token);
            
            Map<String, Object> muleResponse = new HashMap<>();
            muleResponse.put("muleOrderId", "MULE_ORD_" + System.currentTimeMillis());
            muleResponse.put("status", "success");
            
            long duration = System.currentTimeMillis() - startTime;
            
            networkRequestLogger.logOutboundRequest(
                    (String) jsonOrder.get("sessionKey"),
                    "Punchout Gateway",
                    "Mule Service",
                    "POST",
                    "/api/v1/orders",
                    headers,
                    jsonOrder.toString(),
                    200,
                    headers,
                    muleResponse.toString(),
                    duration,
                    "JSON-Order",
                    true,
                    null
            );
            
            return muleResponse;
            
        } catch (Exception e) {
            log.error("Failed to send order to Mule: {}", e.getMessage(), e);
            throw new RuntimeException("Mule request failed: " + e.getMessage(), e);
        }
    }
}
