package com.waters.punchout.exception;

import com.waters.punchout.model.ConversionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CxmlConversionException.class)
    public ResponseEntity<ConversionResponse> handleCxmlConversionException(CxmlConversionException ex) {
        log.error("cXML Conversion error: {}", ex.getMessage(), ex);
        
        ConversionResponse response = ConversionResponse.builder()
                .success(false)
                .message("Conversion error: " + ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ConversionResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid request: {}", ex.getMessage());
        
        ConversionResponse response = ConversionResponse.builder()
                .success(false)
                .message("Invalid request: " + ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConversionResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ConversionResponse response = ConversionResponse.builder()
                .success(false)
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
