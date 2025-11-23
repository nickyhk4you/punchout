package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Build custom Mule payload for Acme customer
     */
    public Map<String, Object> buildMulePayload(PunchOutRequest request) {
        log.debug("Building custom Mule payload for Acme");
        
        Map<String, Object> payload = new HashMap<>();
        
        // Operation
        payload.put("operation", request.getOperation() != null ? request.getOperation().toUpperCase() : "CREATE");
        
        // URLs
        payload.put("return_url", request.getCartReturnUrl());
        payload.put("redirect_url", request.getCartReturnUrl() + "?redirect=1");
        payload.put("sessionId", request.getSessionKey());
        payload.put("previousSid", "");
        payload.put("selectedState", "");
        payload.put("selectedCarrierNumber", "");
        
        // Contact information
        Map<String, Object> contact = new HashMap<>();
        contact.put("firstName", "Procurement");
        contact.put("lastName", "User");
        contact.put("email", "test_astra_freight01@yopmail.com");
        contact.put("phone", "123456789");
        contact.put("soldTo", "144936");
        payload.put("contact", contact);
        
        // Shipping address
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("id", "5120");
        shippingAddress.put("name", "");
        shippingAddress.put("phone", "");
        shippingAddress.put("fax", "");
        
        Map<String, Object> postalAddress = new HashMap<>();
        postalAddress.put("name", "5120");
        postalAddress.put("deliverTo", "1648");
        postalAddress.put("street", "One MedImmune Way");
        postalAddress.put("city", "Gaithersburg");
        postalAddress.put("state", "MD");
        postalAddress.put("postalCode", "20878-2204");
        shippingAddress.put("postalAddress", postalAddress);
        payload.put("shippingAddress", shippingAddress);
        
        // Custom fields
        Map<String, Object> custom = new HashMap<>();
        custom.put("debug", "1");
        custom.put("buyerOrgName", "Astra Zeneca");
        custom.put("country", "US");
        custom.put("currency", "USD");
        custom.put("locale", "en");
        custom.put("freightEnabled", true);
        custom.put("selected_item", "");
        
        // Add extrinsics to custom if present
        if (request.getExtrinsics() != null) {
            request.getExtrinsics().forEach((key, value) -> {
                custom.put(key, value);
            });
        }
        
        payload.put("custom", custom);
        
        log.debug("Acme Mule payload built with {} fields", payload.size());
        return payload;
    }
}
