package com.waters.punchout.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "conversion_rules")
@Data
public class ConversionRuleDocument {
    @Id
    private String id;
    
    @Field("customerId")
    private String customerId;
    
    @Field("documentType")
    private String documentType;
    
    @Field("version")
    private String version;
    
    @Field("active")
    private Boolean active;
    
    @Field("priority")
    private Integer priority;
    
    @Field("fieldMappings")
    private List<FieldMapping> fieldMappings;
    
    @Field("defaultValues")
    private Map<String, Object> defaultValues;
    
    @Field("transformations")
    private List<Transformation> transformations;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("createdBy")
    private String createdBy;
    
    @Field("description")
    private String description;
    
    @Data
    public static class FieldMapping {
        private String sourceXPath;
        private String targetJsonPath;
        private String dataType;
        private String dateFormat;
        private String defaultValue;
        private Boolean required;
        private String valueMapping;
    }
    
    @Data
    public static class Transformation {
        private String type;
        private String targetField;
        private Map<String, String> parameters;
    }
}
