package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SAP Ariba Network Converter
 * Ariba uses AribaNetworkUserId as the credential domain
 * Supports Ariba-specific extrinsics and structure
 */
@Component
@Slf4j
public class AribaV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "ariba";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying SAP Ariba customizations");
        
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        
        // Ariba-specific extrinsics
        if (request.getExtrinsics() != null) {
            String aribaNetworkId = request.getExtrinsics().get("AribaNetworkId");
            String supplierANID = request.getExtrinsics().get("SupplierANID");
            String requisitionerId = request.getExtrinsics().get("RequisitionerId");
            
            log.debug("Ariba Network ID: {}, Supplier ANID: {}, Requisitioner: {}", 
                    aribaNetworkId, supplierANID, requisitionerId);
        }
        
        // Ariba may include phone number
        JsonNode phone = requestNode.path("Contact").path("Phone");
        if (!phone.isMissingNode()) {
            String phoneNumber = phone.path("TelephoneNumber").path("Number").asText();
            log.debug("Ariba contact phone: {}", phoneNumber);
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating SAP Ariba requirements");
        
        // Ariba typically requires UniqueName
        if (request.getExtrinsics() != null) {
            String uniqueName = request.getExtrinsics().get("UniqueName");
            if (uniqueName == null) {
                log.warn("Ariba: UniqueName extrinsic missing (recommended)");
            }
        }
        
        log.debug("Ariba validation passed");
    }
}
