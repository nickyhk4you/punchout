package com.waters.punchout.gateway.exception;

public class PunchOutProcessingException extends RuntimeException {
    
    public PunchOutProcessingException(String message) {
        super(message);
    }
    
    public PunchOutProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
