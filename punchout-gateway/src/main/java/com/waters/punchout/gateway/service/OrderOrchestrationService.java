package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.client.AuthServiceClient;
import com.waters.punchout.gateway.client.MuleServiceClient;
import com.waters.punchout.gateway.converter.CxmlOrderConverter;
import com.waters.punchout.common.dto.OrderResponse;
import com.waters.punchout.gateway.entity.OrderDocument;
import com.waters.punchout.gateway.logging.NetworkRequestLogger;
import com.waters.punchout.gateway.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderOrchestrationService {
    
    private final CxmlOrderConverter orderConverter;
    private final AuthServiceClient authServiceClient;
    private final MuleServiceClient muleServiceClient;
    private final NetworkRequestLogger networkRequestLogger;
    private final OrderRepository orderRepository;
    
    public OrderResponse processOrder(String cxmlContent) {
        log.info("Processing order request");
        
        try {
            OrderDocument order = orderConverter.convertCxmlToOrder(cxmlContent);
            log.info("Parsed order: orderId={}, items={}, total={}", 
                    order.getOrderId(), order.getItems().size(), order.getTotal());
            
            logInboundOrderRequest(cxmlContent, order.getOrderId(), order.getSessionKey());
            
            String token = getAuthToken(order);
            
            Map<String, Object> jsonOrder = orderConverter.convertOrderToJson(order);
            
            Map<String, Object> muleResponse = sendOrderToMule(jsonOrder, token, order.getOrderId());
            
            order.setMuleOrderId((String) muleResponse.get("muleOrderId"));
            order.setStatus("CONFIRMED");
            order.setProcessedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            log.info("Order processed successfully: orderId={}, muleOrderId={}", 
                    order.getOrderId(), order.getMuleOrderId());
            
            return new OrderResponse(order.getOrderId(), order.getMuleOrderId(), "success", 
                    "Order processed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process order: {}", e.getMessage(), e);
            throw new RuntimeException("Order processing failed: " + e.getMessage(), e);
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
