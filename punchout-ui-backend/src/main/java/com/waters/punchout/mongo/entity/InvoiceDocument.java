package com.waters.punchout.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDocument {
    
    @Id
    private String id;
    
    @Field("invoiceNumber")
    private String invoiceNumber;
    
    @Field("orderId")
    private String orderId;
    
    @Field("sessionKey")
    private String sessionKey;
    
    @Field("poNumber")
    private String poNumber;
    
    @Field("routeName")
    private String routeName;
    
    @Field("environment")
    private String environment;
    
    @Field("flags")
    private String flags;
    
    @Field("invoiceTotal")
    private BigDecimal invoiceTotal;
    
    @Field("currency")
    private String currency;
    
    @Field("receivedDate")
    private LocalDateTime receivedDate;
    
    @Field("invoiceDate")
    private LocalDateTime invoiceDate;
    
    @Field("dueDate")
    private LocalDateTime dueDate;
    
    @Field("status")
    private String status;
    
    @Field("customerId")
    private String customerId;
    
    @Field("customerName")
    private String customerName;
    
    @Field("supplierName")
    private String supplierName;
    
    @Field("taxAmount")
    private BigDecimal taxAmount;
    
    @Field("shippingAmount")
    private BigDecimal shippingAmount;
    
    @Field("subtotal")
    private BigDecimal subtotal;
    
    @Field("shipTo")
    private Map<String, Object> shipTo;
    
    @Field("billTo")
    private Map<String, Object> billTo;
    
    @Field("lineItems")
    private List<Map<String, Object>> lineItems;
    
    @Field("paymentTerms")
    private String paymentTerms;
    
    @Field("notes")
    private String notes;
    
    @Field("processedAt")
    private LocalDateTime processedAt;
    
    @Field("paidAt")
    private LocalDateTime paidAt;
    
    @Field("source")
    private String source;
}
