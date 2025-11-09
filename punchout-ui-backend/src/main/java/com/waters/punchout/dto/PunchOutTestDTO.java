package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutTestDTO {
    
    private String id;
    private String testName;
    private String catalogRouteId;
    private String catalogRouteName;
    private String environment;
    private String tester;
    private LocalDateTime testDate;
    private String status;
    private LocalDateTime setupRequestSent;
    private LocalDateTime setupResponseReceived;
    private String catalogUrl;
    private LocalDateTime orderMessageSent;
    private LocalDateTime orderMessageReceived;
    private Long totalDuration;
    private String setupRequest;
    private String setupResponse;
    private String orderMessage;
    private String orderResponse;
    private String errorMessage;
    private String notes;
}
