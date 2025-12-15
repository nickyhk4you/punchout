package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.NetworkRequestDTO;
import com.waters.punchout.dto.OrderDTO;
import com.waters.punchout.mongo.service.NetworkRequestMongoService;
import com.waters.punchout.mongo.service.OrderMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String environment
    ) {
        log.info("GET /api/v1/orders/download - status={}, customerId={}, environment={}", status, customerId, environment);
        
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
        
        String csvContent = generateCsv(orders);
        byte[] csvBytes = csvContent.getBytes();
        
        String filename = "orders_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        
        log.info("Downloading {} orders as CSV", orders.size());
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
    
    private String generateCsv(List<OrderDTO> orders) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Order ID,Customer ID,Customer Name,Order Date,Status,Environment,Total,Currency,Tax Amount,Items Count,Order Type,Source,Dialect\n");
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (OrderDTO order : orders) {
            csv.append(escapeCsvField(order.getOrderId())).append(",");
            csv.append(escapeCsvField(order.getCustomerId())).append(",");
            csv.append(escapeCsvField(order.getCustomerName())).append(",");
            csv.append(order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "").append(",");
            csv.append(escapeCsvField(order.getStatus())).append(",");
            csv.append(escapeCsvField(order.getEnvironment())).append(",");
            csv.append(order.getTotal() != null ? order.getTotal().toString() : "0").append(",");
            csv.append(escapeCsvField(order.getCurrency())).append(",");
            csv.append(order.getTaxAmount() != null ? order.getTaxAmount().toString() : "0").append(",");
            csv.append(order.getItems() != null ? order.getItems().size() : 0).append(",");
            csv.append(escapeCsvField(order.getOrderType())).append(",");
            csv.append(escapeCsvField(order.getSource())).append(",");
            csv.append(escapeCsvField(order.getDialect())).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
