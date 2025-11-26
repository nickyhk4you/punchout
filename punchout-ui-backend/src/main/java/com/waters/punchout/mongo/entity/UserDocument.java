package com.waters.punchout.mongo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
@Data
public class UserDocument {
    
    @Id
    private String id;
    
    @Field("userId")
    private String userId;
    
    @Field("username")
    private String username;
    
    @Field("email")
    private String email;
    
    @Field("firstName")
    private String firstName;
    
    @Field("lastName")
    private String lastName;
    
    @Field("role")
    private String role; // ADMIN, MANAGER, DEVELOPER, USER, VIEWER
    
    @Field("status")
    private String status; // ACTIVE, INACTIVE, PENDING, SUSPENDED
    
    @Field("department")
    private String department;
    
    @Field("phoneNumber")
    private String phoneNumber;
    
    @Field("jobTitle")
    private String jobTitle;
    
    @Field("permissions")
    private List<String> permissions;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("lastLoginAt")
    private LocalDateTime lastLoginAt;
    
    @Field("passwordHash")
    private String passwordHash;
    
    @Field("passwordChangedAt")
    private LocalDateTime passwordChangedAt;
    
    @Field("isEmailVerified")
    private Boolean isEmailVerified;
    
    @Field("isTwoFactorEnabled")
    private Boolean isTwoFactorEnabled;
    
    @Field("preferences")
    private Map<String, Object> preferences;
    
    // Computed field
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
