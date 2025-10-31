package com.waters.punchout.controller;

import com.waters.punchout.model.ConversionResponse;
import com.waters.punchout.model.CxmlRequest;
import com.waters.punchout.service.CxmlConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cxml")
@RequiredArgsConstructor
public class CxmlConversionController {

    private final CxmlConversionService conversionService;

    @PostMapping(value = "/convert", 
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionResponse> convertCxml(@RequestBody CxmlRequest request) {
        log.info("Received cXML conversion request for customer: {}, documentType: {}", 
                request.getCustomerId(), 
                request.getDocumentType());
        
        ConversionResponse response = conversionService.convertCxml(request);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping(value = "/convert/{customerId}/{documentType}",
                 consumes = MediaType.TEXT_XML_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionResponse> convertCxmlRaw(
            @PathVariable String customerId,
            @PathVariable String documentType,
            @RequestBody String cxmlContent) {
        
        log.info("Received raw cXML conversion request for customer: {}, documentType: {}", 
                customerId, 
                documentType);
        
        CxmlRequest request = CxmlRequest.builder()
                .customerId(customerId)
                .documentType(documentType)
                .cxmlContent(cxmlContent)
                .build();
        
        ConversionResponse response = conversionService.convertCxml(request);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("cXML Conversion Service is running");
    }
}
