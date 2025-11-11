package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "environment_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentConfig {
    
    @Id
    private String id;
    
    @Field("environment")
    private String environment; // dev, stage, prod, s4-dev
    
    @Field("authServiceUrl")
    private String authServiceUrl;
    
    @Field("muleServiceUrl")
    private String muleServiceUrl;
    
    @Field("catalogBaseUrl")
    private String catalogBaseUrl;
    
    @Field("description")
    private String description;
    
    @Field("enabled")
    private Boolean enabled;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("createdBy")
    private String createdBy;
    
    @Field("updatedBy")
    private String updatedBy;
}
