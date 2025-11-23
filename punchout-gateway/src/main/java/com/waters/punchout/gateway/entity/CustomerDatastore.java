package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "customer_datastore")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDatastore {
    
    @Id
    private String id;
    
    @Field("customer")
    private String customer;
    
    @Field("environment")
    private String environment;
    
    @Field("keyValuePairs")
    private Map<String, String> keyValuePairs;
    
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
