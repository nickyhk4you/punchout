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

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocument {
    
    @Id
    private String id;
    
    @Field("orderId")
    private String orderId;
    
    @Field("sessionKey")
    private String sessionKey;
    
    @Field("orderDate")
    private LocalDateTime orderDate;
    
    @Field("orderType")
    private String orderType;
    
    @Field("orderVersion")
    private String orderVersion;
    
    @Field("customerId")
    private String customerId;
    
    @Field("customerName")
    private String customerName;
    
    @Field("total")
    private BigDecimal total;
    
    @Field("currency")
    private String currency;
    
    @Field("taxAmount")
    private BigDecimal taxAmount;
    
    @Field("shipTo")
    private Map<String, Object> shipTo;
    
    @Field("billTo")
    private Map<String, Object> billTo;
    
    @Field("items")
    private List<Map<String, Object>> items;
    
    @Field("extrinsics")
    private Map<String, String> extrinsics;
    
    @Field("comments")
    private String comments;
    
    @Field("status")
    private String status;
    
    @Field("receivedAt")
    private LocalDateTime receivedAt;
    
    @Field("processedAt")
    private LocalDateTime processedAt;
    
    @Field("muleOrderId")
    private String muleOrderId;
    
    @Field("environment")
    private String environment;
    
    @Field("source")
    private String source;
    
    @Field("dialect")
    private String dialect;
}
