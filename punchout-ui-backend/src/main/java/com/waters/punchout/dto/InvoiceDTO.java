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
public class InvoiceDTO {
    
    private String id;
    private String invoiceNumber;
    private String orderId;
    private String sessionKey;
    private String poNumber;
    private String routeName;
    private String environment;
    private String flags;
    private BigDecimal invoiceTotal;
    private String currency;
    private LocalDateTime receivedDate;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private String status;
    private String customerId;
    private String customerName;
    private String supplierName;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal subtotal;
    private Map<String, Object> shipTo;
    private Map<String, Object> billTo;
    private List<Map<String, Object>> lineItems;
    private String paymentTerms;
    private String notes;
    private LocalDateTime processedAt;
    private LocalDateTime paidAt;
    private String source;
}
