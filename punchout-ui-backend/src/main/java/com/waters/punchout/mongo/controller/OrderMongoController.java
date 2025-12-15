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
    
    @GetMapping("/{orderId}/download")
    public ResponseEntity<byte[]> downloadSingleOrder(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{}/download", orderId);
        
        OrderDTO order = orderService.getOrderByOrderId(orderId);
        
        if (order == null) {
            log.warn("Order not found for download: {}", orderId);
            return ResponseEntity.notFound().build();
        }
        
        String csvContent = generateSingleOrderCsv(order);
        byte[] csvBytes = csvContent.getBytes();
        
        String filename = "order_" + orderId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        
        log.info("Downloading order {} as CSV", orderId);
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
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
    
    private String generateSingleOrderCsv(OrderDTO order) {
        StringBuilder csv = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        csv.append("ORDER DETAILS\n");
        csv.append("Field,Value\n");
        csv.append("Order ID,").append(escapeCsvField(order.getOrderId())).append("\n");
        csv.append("Customer ID,").append(escapeCsvField(order.getCustomerId())).append("\n");
        csv.append("Customer Name,").append(escapeCsvField(order.getCustomerName())).append("\n");
        csv.append("Order Date,").append(order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "").append("\n");
        csv.append("Order Type,").append(escapeCsvField(order.getOrderType())).append("\n");
        csv.append("Status,").append(escapeCsvField(order.getStatus())).append("\n");
        csv.append("Environment,").append(escapeCsvField(order.getEnvironment())).append("\n");
        csv.append("Total,").append(order.getTotal() != null ? order.getTotal().toString() : "0").append("\n");
        csv.append("Currency,").append(escapeCsvField(order.getCurrency())).append("\n");
        csv.append("Tax Amount,").append(order.getTaxAmount() != null ? order.getTaxAmount().toString() : "0").append("\n");
        csv.append("Source,").append(escapeCsvField(order.getSource())).append("\n");
        csv.append("Dialect,").append(escapeCsvField(order.getDialect())).append("\n");
        csv.append("Session Key,").append(escapeCsvField(order.getSessionKey())).append("\n");
        csv.append("Mule Order ID,").append(escapeCsvField(order.getMuleOrderId())).append("\n");
        
        if (order.getShipTo() != null) {
            csv.append("\nSHIPPING ADDRESS\n");
            csv.append("Field,Value\n");
            order.getShipTo().forEach((key, value) -> 
                csv.append(escapeCsvField(key)).append(",").append(escapeCsvField(String.valueOf(value))).append("\n")
            );
        }
        
        if (order.getBillTo() != null) {
            csv.append("\nBILLING ADDRESS\n");
            csv.append("Field,Value\n");
            order.getBillTo().forEach((key, value) -> 
                csv.append(escapeCsvField(key)).append(",").append(escapeCsvField(String.valueOf(value))).append("\n")
            );
        }
        
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            csv.append("\nLINE ITEMS\n");
            csv.append("Line Number,Part Number,Description,Quantity,Unit Price,Currency,Extended Amount\n");
            
            for (Map<String, Object> item : order.getItems()) {
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("lineNumber", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("supplierPartId", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("description", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("quantity", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("unitPrice", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("currency", "")))).append(",");
                csv.append(escapeCsvField(String.valueOf(item.getOrDefault("extendedAmount", "")))).append("\n");
            }
        }
        
        if (order.getExtrinsics() != null && !order.getExtrinsics().isEmpty()) {
            csv.append("\nEXTRINSICS\n");
            csv.append("Key,Value\n");
            order.getExtrinsics().forEach((key, value) -> 
                csv.append(escapeCsvField(key)).append(",").append(escapeCsvField(value)).append("\n")
            );
        }
        
        return csv.toString();
    }
}
