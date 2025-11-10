package com.waters.punchout.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "cxml_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CxmlTemplateDocument {
    
    @Id
    private String id;
    
    @Field("templateName")
    private String templateName;
    
    @Field("environment")
    private String environment;  // DEV, STAGE, PROD, S4-DEV
    
    @Field("customerId")
    private String customerId;
    
    @Field("customerName")
    private String customerName;
    
    @Field("cxmlTemplate")
    private String cxmlTemplate;
    
    @Field("description")
    private String description;
    
    @Field("isDefault")
    private Boolean isDefault;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("createdBy")
    private String createdBy;
}
