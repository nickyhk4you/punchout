package com.waters.punchout.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {
    private String orderId;
    private String customerId;
    private String orderDate;
    private String buyerInfo;
    private String supplierInfo;
    private List<OrderItem> items;
    private Map<String, Object> additionalData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String lineNumber;
        private String itemId;
        private String description;
        private Integer quantity;
        private Double unitPrice;
        private String uom;
    }
}
