package com.waters.punchout.gateway.converter.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.model.PunchOutRequest;

public interface PunchOutConverterStrategy {
    
    String customerId();
    
    String version();
    
    PunchOutRequest convert(JsonNode root, ConversionContext ctx) throws Exception;
    
    default void validate(PunchOutRequest request, ConversionContext ctx) {
        // Default: no validation
    }
}
