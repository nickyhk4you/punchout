package com.waters.punchout.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "catalog_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogRouteDocument {
    
    @Id
    private String id;
    
    @Field("routeName")
    private String routeName;
    
    @Field("domain")
    private String domain;
    
    @Field("network")
    private String network; // e.g., "Ariba, Inc.", "SAP", "Oracle"
    
    @Field("type")
    private String type; // e.g., "cxml", "oci", "rest"
    
    @Field("description")
    private String description;
    
    @Field("environments")
    private List<EnvironmentConfig> environments;
    
    @Field("active")
    private Boolean active;
    
    @Field("createdDate")
    private LocalDateTime createdDate;
    
    @Field("lastModified")
    private LocalDateTime lastModified;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentConfig {
        private String environment; // production, staging, development, test
        private String url;
        private String username;
        private String password;
        private String sharedSecret;
        private Boolean enabled;
        private String notes;
    }
}
