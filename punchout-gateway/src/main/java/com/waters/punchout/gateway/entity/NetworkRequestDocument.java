package com.waters.punchout.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "network_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkRequestDocument {
    
    @Id
    private String id;
    
    @Field("sessionKey")
    private String sessionKey;
    
    @Field("orderId")
    private String orderId;
    
    @Field("requestId")
    private String requestId;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("direction")
    private String direction; // INBOUND or OUTBOUND
    
    @Field("source")
    private String source;
    
    @Field("destination")
    private String destination;
    
    @Field("method")
    private String method;
    
    @Field("url")
    private String url;
    
    @Field("headers")
    private Map<String, String> headers;
    
    @Field("requestBody")
    private String requestBody;
    
    @Field("statusCode")
    private Integer statusCode;
    
    @Field("responseHeaders")
    private Map<String, String> responseHeaders;
    
    @Field("responseBody")
    private String responseBody;
    
    @Field("duration")
    private Long duration;
    
    @Field("requestType")
    private String requestType;
    
    @Field("success")
    private Boolean success;
    
    @Field("errorMessage")
    private String errorMessage;
}
