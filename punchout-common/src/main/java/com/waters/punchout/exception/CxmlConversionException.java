package com.waters.punchout.exception;

public class CxmlConversionException extends RuntimeException {
    public CxmlConversionException(String message) {
        super(message);
    }

    public CxmlConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
