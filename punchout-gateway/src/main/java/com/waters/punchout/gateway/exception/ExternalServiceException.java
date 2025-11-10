package com.waters.punchout.gateway.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    private final int statusCode;
    
    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super(String.format("External service '%s' failed with status %d: %s", serviceName, statusCode, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, int statusCode, String message, Throwable cause) {
        super(String.format("External service '%s' failed with status %d: %s", serviceName, statusCode, message), cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("External service '%s' failed: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.statusCode = 0;
    }
}
