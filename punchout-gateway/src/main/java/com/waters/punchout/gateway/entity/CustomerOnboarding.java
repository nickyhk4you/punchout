package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "customer_onboarding")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOnboarding {
    
    @Id
    private String id;
    
    @Field("customerName")
    private String customerName;
    
    @Field("customerType")
    private String customerType;
    
    @Field("network")
    private String network;
    
    @Field("environment")
    private String environment;
    
    @Field("sampleCxml")
    private String sampleCxml;
    
    @Field("targetJson")
    private String targetJson;
    
    @Field("fieldMappings")
    private Map<String, String> fieldMappings;
    
    @Field("notes")
    private String notes;
    
    @Field("converterClass")
    private String converterClass;
    
    @Field("status")
    private String status;
    
    @Field("deployed")
    private Boolean deployed;
    
    @Field("deployedAt")
    private LocalDateTime deployedAt;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("createdBy")
    private String createdBy;
    
    @Field("updatedBy")
    private String updatedBy;
}
