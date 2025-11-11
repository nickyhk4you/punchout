package com.waters.punchout.gateway.converter.dialect;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DialectDetector {
    
    public Dialect detect(JsonNode root) {
        String userAgent = root.path("Header").path("Sender").path("UserAgent").asText("").toLowerCase();
        
        if (userAgent.contains("ariba")) {
            log.debug("Detected Ariba dialect from UserAgent: {}", userAgent);
            return Dialect.ARIBA;
        }
        
        if (userAgent.contains("oci") || userAgent.contains("sap")) {
            log.debug("Detected OCI/SAP dialect from UserAgent: {}", userAgent);
            return Dialect.OCI;
        }
        
        log.debug("Using CXML dialect for UserAgent: {}", userAgent);
        return Dialect.CXML;
    }
}
