package com.waters.punchout.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "punchout_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutTestDocument {
    
    @Id
    private String id;
    
    @Field("testName")
    private String testName;
    
    @Field("catalogRouteId")
    private String catalogRouteId;
    
    @Field("catalogRouteName")
    private String catalogRouteName;
    
    @Field("environment")
    private String environment;
    
    @Field("tester")
    private String tester; // Developer name/email
    
    @Field("testDate")
    private LocalDateTime testDate;
    
    @Field("status")
    private String status; // RUNNING, SUCCESS, FAILED, CANCELLED
    
    @Field("setupRequestSent")
    private LocalDateTime setupRequestSent;
    
    @Field("setupResponseReceived")
    private LocalDateTime setupResponseReceived;
    
    @Field("catalogUrl")
    private String catalogUrl;
    
    @Field("orderMessageSent")
    private LocalDateTime orderMessageSent;
    
    @Field("orderMessageReceived")
    private LocalDateTime orderMessageReceived;
    
    @Field("totalDuration")
    private Long totalDuration; // milliseconds
    
    @Field("setupRequest")
    private String setupRequest;
    
    @Field("setupResponse")
    private String setupResponse;
    
    @Field("orderMessage")
    private String orderMessage;
    
    @Field("orderResponse")
    private String orderResponse;
    
    @Field("errorMessage")
    private String errorMessage;
    
    @Field("notes")
    private String notes;
}
