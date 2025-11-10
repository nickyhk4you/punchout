package com.waters.punchout.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler({CxmlParsingException.class, PunchOutProcessingException.class, ExternalServiceException.class})
    public ResponseEntity<String> handlePunchOutExceptions(RuntimeException ex) {
        log.error("PunchOut error occurred: {}", ex.getMessage(), ex);
        
        // Use generic message for security, log details internally
        String safeMessage = "PunchOut processing failed. Please contact support.";
        
        // Build cXML error response with escaped message
        String cxmlError = buildCxmlErrorResponse(safeMessage);
        
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_XML)
                .body(cxmlError);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        String safeMessage = "An unexpected error occurred. Please contact support.";
        String cxmlError = buildCxmlErrorResponse(safeMessage);
        
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_XML)
                .body(cxmlError);
    }
    
    private String buildCxmlErrorResponse(String errorMessage) {
        String escapedMessage = StringEscapeUtils.escapeXml11(errorMessage);
        
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<cXML>\n" +
            "  <Response>\n" +
            "    <Status code=\"500\" text=\"error\">%s</Status>\n" +
            "  </Response>\n" +
            "</cXML>",
            escapedMessage
        );
    }
}
