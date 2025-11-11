package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "default";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating default conversion");
        
        if (request.getBuyerCookie() == null || request.getBuyerCookie().isEmpty()) {
            throw new IllegalArgumentException("BuyerCookie is required");
        }
        
        if (request.getSessionKey() == null || request.getSessionKey().isEmpty()) {
            throw new IllegalArgumentException("SessionKey is required");
        }
    }
}
