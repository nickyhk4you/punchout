package com.waters.punchout.gateway.exception;

public class CxmlParsingException extends RuntimeException {
    
    public CxmlParsingException(String message) {
        super(message);
    }
    
    public CxmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
