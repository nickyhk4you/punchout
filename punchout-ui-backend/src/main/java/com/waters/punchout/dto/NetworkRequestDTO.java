package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkRequestDTO {
    
    private String id;
    private String sessionKey;
    private String orderId;
    private String requestId;
    private LocalDateTime timestamp;
    private String direction;
    private String source;
    private String destination;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String requestBody;
    private Integer statusCode;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Long duration;
    private String requestType;
    private Boolean success;
    private String errorMessage;
}
