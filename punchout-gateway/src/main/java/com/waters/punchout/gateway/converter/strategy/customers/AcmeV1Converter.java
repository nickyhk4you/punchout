package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcmeV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "acme";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying Acme-specific customizations");
        
        // Acme uses composite session key: BuyerCookie-ToIdentity
        String toIdentity = request.getToIdentity();
        if (toIdentity != null && !toIdentity.isEmpty()) {
            String customSessionKey = request.getBuyerCookie() + "-" + toIdentity;
            request.setSessionKey(customSessionKey);
            log.debug("Acme composite session key: {}", customSessionKey);
        }
        
        // Log Acme-specific extrinsics
        if (request.getExtrinsics() != null) {
            String costCenter = request.getExtrinsics().get("CostCenter");
            String department = request.getExtrinsics().get("Department");
            log.debug("Acme CostCenter: {}, Department: {}", costCenter, department);
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating Acme requirements");
        
        // Require CostCenter extrinsic
        if (request.getExtrinsics() == null || 
            request.getExtrinsics().get("CostCenter") == null) {
            log.warn("Acme validation: CostCenter extrinsic missing");
            // Note: Just warning for now, can throw exception if strict validation needed
        }
        
        // Require From identity
        if (request.getFromIdentity() == null || request.getFromIdentity().isEmpty()) {
            throw new IllegalArgumentException("Acme requires From identity (buyer ID)");
        }
        
        log.debug("Acme validation passed");
    }
}
