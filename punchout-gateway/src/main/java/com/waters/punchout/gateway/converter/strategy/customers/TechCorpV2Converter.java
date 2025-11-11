package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TechCorpV2Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "techcorp";
    }
    
    @Override
    public String version() {
        return "v2";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying TechCorp v2 customizations");
        
        // TechCorp may use EmailAddress instead of Email
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        JsonNode emailAddress = requestNode.path("Contact").path("EmailAddress");
        if (!emailAddress.isMissingNode()) {
            request.setContactEmail(emailAddress.asText());
            log.debug("TechCorp email from EmailAddress field: {}", emailAddress.asText());
        }
        
        // Log TechCorp-specific extrinsics
        if (request.getExtrinsics() != null) {
            String companyCode = request.getExtrinsics().get("CompanyCode");
            String uniqueName = request.getExtrinsics().get("UniqueName");
            log.debug("TechCorp CompanyCode: {}, UniqueName: {}", companyCode, uniqueName);
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating TechCorp v2 requirements");
        
        if (request.getExtrinsics() == null) {
            throw new IllegalArgumentException("TechCorp requires extrinsics");
        }
        
        // Require CompanyCode
        if (request.getExtrinsics().get("CompanyCode") == null) {
            log.warn("TechCorp validation: CompanyCode extrinsic missing");
        }
        
        // Require UniqueName
        if (request.getExtrinsics().get("UniqueName") == null) {
            log.warn("TechCorp validation: UniqueName extrinsic missing");
        }
        
        log.debug("TechCorp validation passed");
    }
}
