package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Coupa Procurement Platform Converter
 * Coupa uses its own variant of cXML with specific extrinsics
 * Supports Coupa-specific fields and validation
 */
@Component
@Slf4j
public class CoupaV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "coupa";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying Coupa customizations");
        
        // Coupa-specific extrinsics
        if (request.getExtrinsics() != null) {
            String coupaVersion = request.getExtrinsics().get("CoupaVersion");
            String requesterEmail = request.getExtrinsics().get("requester-email");
            String requesterLogin = request.getExtrinsics().get("requester-login");
            String buyerPartNum = request.getExtrinsics().get("buyer-part-num");
            
            log.debug("Coupa Version: {}, Requester: {}, Login: {}, Part#: {}", 
                    coupaVersion, requesterEmail, requesterLogin, buyerPartNum);
            
            // Coupa session key format: Include requester login
            if (requesterLogin != null) {
                String coupaSessionKey = request.getBuyerCookie() + "-" + requesterLogin;
                request.setSessionKey(coupaSessionKey);
                log.debug("Coupa session key with login: {}", coupaSessionKey);
            }
        }
        
        // Coupa may use different contact structure
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        JsonNode contact = requestNode.path("Contact");
        
        // Try both Email and email fields (Coupa can vary)
        JsonNode email = contact.path("Email");
        if (email.isMissingNode()) {
            email = contact.path("email");
        }
        if (!email.isMissingNode()) {
            request.setContactEmail(email.asText());
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating Coupa requirements");
        
        // Coupa typically requires requester-email
        if (request.getExtrinsics() != null) {
            String requesterEmail = request.getExtrinsics().get("requester-email");
            if (requesterEmail == null) {
                log.warn("Coupa: requester-email extrinsic missing (recommended)");
            }
        }
        
        log.debug("Coupa validation passed");
    }
}
