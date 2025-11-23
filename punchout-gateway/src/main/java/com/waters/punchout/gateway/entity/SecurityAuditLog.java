package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "security_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditLog {
    
    @Id
    private String id;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("eventType")
    private String eventType; // AUTH_SUCCESS, AUTH_FAILURE, API_KEY_GENERATED, API_KEY_REVOKED, CONFIG_CHANGED
    
    @Field("severity")
    private String severity; // INFO, WARNING, ERROR, CRITICAL
    
    @Field("customerName")
    private String customerName;
    
    @Field("ipAddress")
    private String ipAddress;
    
    @Field("userAgent")
    private String userAgent;
    
    @Field("description")
    private String description;
    
    @Field("metadata")
    private Map<String, String> metadata;
    
    @Field("userId")
    private String userId;
    
    @Field("sessionKey")
    private String sessionKey;
    
    @Field("apiKey")
    private String apiKey;
}
