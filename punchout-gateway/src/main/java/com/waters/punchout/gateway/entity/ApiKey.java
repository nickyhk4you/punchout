package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    
    @Id
    private String id;
    
    @Field("keyValue")
    private String keyValue;
    
    @Field("customerName")
    private String customerName;
    
    @Field("description")
    private String description;
    
    @Field("permissions")
    private List<String> permissions; // PUNCHOUT, ORDER, INVOICE, READ, WRITE
    
    @Field("environment")
    private String environment;
    
    @Field("enabled")
    private Boolean enabled;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("expiresAt")
    private LocalDateTime expiresAt;
    
    @Field("lastUsedAt")
    private LocalDateTime lastUsedAt;
    
    @Field("usageCount")
    private Integer usageCount;
    
    @Field("createdBy")
    private String createdBy;
    
    @Field("revokedAt")
    private LocalDateTime revokedAt;
    
    @Field("revokedBy")
    private String revokedBy;
}
