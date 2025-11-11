package com.waters.punchout.gateway.converter.resolve;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.dialect.Dialect;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversionContext {
    private final ConversionKey key;
    private final Dialect dialect;
    private final JsonNode normalizedRoot;
    private final String originalCxml;
    
    public CustomerConfig getCustomerConfig() {
        return key != null ? key.getCustomerConfig() : null;
    }
}
