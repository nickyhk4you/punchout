package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String orderId;
    private String sessionKey;
    private LocalDateTime orderDate;
    private String orderType;
    private String orderVersion;
    private String customerId;
    private String customerName;
    private BigDecimal total;
    private String currency;
    private BigDecimal taxAmount;
    private Map<String, Object> shipTo;
    private Map<String, Object> billTo;
    private List<Map<String, Object>> items;
    private Map<String, String> extrinsics;
    private String comments;
    private String status;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private String muleOrderId;
    private String environment;
    private String source;
    private String dialect;
}
