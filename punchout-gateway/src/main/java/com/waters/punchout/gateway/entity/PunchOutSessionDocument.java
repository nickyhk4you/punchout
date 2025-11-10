package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "punchout")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutSessionDocument {
    
    @Id
    private String id;
    
    @Field("sessionKey")
    private String sessionKey;
    
    @Field("cartReturn")
    private String cartReturn;
    
    @Field("operation")
    private String operation;
    
    @Field("contact")
    private String contact;
    
    @Field("routeName")
    private String routeName;
    
    @Field("environment")
    private String environment;
    
    @Field("flags")
    private String flags;
    
    @Field("sessionDate")
    private LocalDateTime sessionDate;
    
    @Field("punchedIn")
    private LocalDateTime punchedIn;
    
    @Field("punchedOut")
    private LocalDateTime punchedOut;
    
    @Field("orderId")
    private String orderId;
    
    @Field("orderValue")
    private BigDecimal orderValue;
    
    @Field("lineItems")
    private Integer lineItems;
    
    @Field("itemQuantity")
    private Integer itemQuantity;
    
    @Field("catalog")
    private String catalog;
    
    @Field("network")
    private String network;
    
    @Field("parser")
    private String parser;
    
    @Field("buyerCookie")
    private String buyerCookie;
}
