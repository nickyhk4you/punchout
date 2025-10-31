package com.waters.punchout.service.impl;

import com.waters.punchout.converter.CxmlConverter;
import com.waters.punchout.model.ConversionResponse;
import com.waters.punchout.model.CxmlRequest;
import com.waters.punchout.service.CxmlConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CxmlConversionServiceImpl implements CxmlConversionService {

    private final Map<String, CxmlConverter> converterMap;

    public CxmlConversionServiceImpl(List<CxmlConverter> converters) {
        this.converterMap = converters.stream()
                .collect(Collectors.toMap(
                        converter -> converter.getCustomerId().toUpperCase(),
                        Function.identity()
                ));
        log.info("Initialized {} customer converters: {}", 
                converterMap.size(), 
                converterMap.keySet());
    }

    @Override
    public ConversionResponse convertCxml(CxmlRequest request) {
        try {
            validateRequest(request);
            
            CxmlConverter converter = getConverter(request.getCustomerId());
            
            Object jsonData = converter.convert(
                    request.getCxmlContent(), 
                    request.getDocumentType()
            );
            
            return ConversionResponse.builder()
                    .success(true)
                    .message("Conversion successful")
                    .data(jsonData)
                    .customerId(request.getCustomerId())
                    .documentType(request.getDocumentType())
                    .build();
                    
        } catch (Exception e) {
            log.error("Conversion failed for customer: {}", request.getCustomerId(), e);
            return ConversionResponse.builder()
                    .success(false)
                    .message("Conversion failed: " + e.getMessage())
                    .customerId(request.getCustomerId())
                    .documentType(request.getDocumentType())
                    .build();
        }
    }

    private CxmlConverter getConverter(String customerId) {
        String customerKey = customerId != null ? customerId.toUpperCase() : "DEFAULT";
        
        CxmlConverter converter = converterMap.get(customerKey);
        if (converter == null) {
            log.warn("No converter found for customer: {}, using DEFAULT", customerId);
            converter = converterMap.get("DEFAULT");
        }
        
        if (converter == null) {
            throw new IllegalStateException("No default converter configured");
        }
        
        return converter;
    }

    private void validateRequest(CxmlRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getCxmlContent() == null || request.getCxmlContent().trim().isEmpty()) {
            throw new IllegalArgumentException("cXML content cannot be empty");
        }
        if (request.getDocumentType() == null || request.getDocumentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be empty");
        }
    }
}
