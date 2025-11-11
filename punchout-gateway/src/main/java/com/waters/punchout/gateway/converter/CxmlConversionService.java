package com.waters.punchout.gateway.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.gateway.converter.dialect.Dialect;
import com.waters.punchout.gateway.converter.dialect.DialectDetector;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.resolve.ConversionKey;
import com.waters.punchout.gateway.converter.resolve.CustomerResolver;
import com.waters.punchout.gateway.converter.strategy.ConverterRegistry;
import com.waters.punchout.gateway.converter.strategy.PunchOutConverterStrategy;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CxmlConversionService {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    private final DialectDetector dialectDetector;
    private final CustomerResolver customerResolver;
    private final ConverterRegistry converterRegistry;
    
    public PunchOutRequest convertCxmlToRequest(String cxmlContent) throws Exception {
        log.info("Starting cXML conversion");
        
        try {
            // Step 1: Parse cXML to JsonNode
            JsonNode root = xmlMapper.readTree(cxmlContent);
            log.debug("Parsed cXML to JsonNode");
            
            // Step 2: Detect dialect
            Dialect dialect = dialectDetector.detect(root);
            log.debug("Detected dialect: {}", dialect);
            
            // Step 3: Resolve customer (could normalize by dialect here if needed)
            JsonNode normalized = root; // For now, no dialect normalization
            ConversionKey key = customerResolver.resolve(normalized);
            log.info("Resolved to customer: {} version: {}", key.getCustomerId(), key.getVersion());
            
            // Step 4: Get converter strategy
            PunchOutConverterStrategy strategy = converterRegistry.get(key);
            
            // Step 5: Create context
            ConversionContext context = new ConversionContext(key, dialect, normalized, cxmlContent);
            
            // Step 6: Execute conversion
            PunchOutRequest request = strategy.convert(normalized, context);
            
            log.info("Successfully converted cXML for customer: {}, sessionKey: {}", 
                    key.getCustomerId(), request.getSessionKey());
            
            return request;
            
        } catch (Exception e) {
            log.error("Failed to convert cXML: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse cXML request: " + e.getMessage(), e);
        }
    }
}
