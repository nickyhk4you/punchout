package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "global";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying Global Solutions customizations");
        
        // Log Global-specific extrinsics
        if (request.getExtrinsics() != null) {
            String region = request.getExtrinsics().get("Region");
            log.debug("Global Solutions Region: {}", region);
            
            // Validate region if present
            if (region != null) {
                if (!region.matches("EMEA|APAC|AMER")) {
                    log.warn("Global Solutions: Invalid region {}, expected EMEA/APAC/AMER", region);
                }
            }
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating Global Solutions requirements");
        
        // Require Region extrinsic
        if (request.getExtrinsics() == null || 
            request.getExtrinsics().get("Region") == null) {
            log.warn("Global Solutions validation: Region extrinsic missing");
        }
        
        log.debug("Global Solutions validation passed");
    }
}
