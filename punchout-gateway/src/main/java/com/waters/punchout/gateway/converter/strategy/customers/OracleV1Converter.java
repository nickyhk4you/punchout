package com.waters.punchout.gateway.converter.strategy.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.waters.punchout.gateway.converter.resolve.ConversionContext;
import com.waters.punchout.gateway.converter.strategy.BaseConverter;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Oracle iProcurement / Oracle Procurement Cloud Converter
 * Oracle uses specific credential formats and extrinsics
 * Supports both Oracle E-Business Suite and Oracle Cloud
 */
@Component
@Slf4j
public class OracleV1Converter extends BaseConverter {
    
    @Override
    public String customerId() {
        return "oracle";
    }
    
    @Override
    public String version() {
        return "v1";
    }
    
    @Override
    protected void customize(PunchOutRequest request, JsonNode root, ConversionContext ctx) {
        log.debug("Applying Oracle iProcurement customizations");
        
        // Oracle-specific extrinsics
        if (request.getExtrinsics() != null) {
            String orgId = request.getExtrinsics().get("OrgId");
            String orgCode = request.getExtrinsics().get("OrgCode");
            String userId = request.getExtrinsics().get("UserId");
            String respId = request.getExtrinsics().get("RespId");
            String applicationId = request.getExtrinsics().get("ApplicationId");
            
            log.debug("Oracle Org ID: {}, Org Code: {}, User ID: {}, Resp ID: {}, App ID: {}", 
                    orgId, orgCode, userId, respId, applicationId);
            
            // Oracle session key format: Include OrgId and UserId
            if (orgId != null && userId != null) {
                String oracleSessionKey = request.getBuyerCookie() + "-ORG" + orgId + "-USER" + userId;
                request.setSessionKey(oracleSessionKey);
                log.debug("Oracle session key: {}", oracleSessionKey);
            }
        }
        
        // Oracle may use different cart return URL format
        JsonNode requestNode = root.path("Request").path("PunchOutSetupRequest");
        JsonNode browserPost = requestNode.path("BrowserFormPost");
        
        // Check for Oracle-specific URL structure
        if (!browserPost.isMissingNode()) {
            String url = browserPost.path("URL").asText();
            if (url.contains("OAFunc") || url.contains("iProcurement")) {
                log.debug("Detected Oracle iProcurement return URL: {}", url);
            }
        }
    }
    
    @Override
    public void validate(PunchOutRequest request, ConversionContext ctx) {
        log.debug("Validating Oracle requirements");
        
        // Oracle requires specific extrinsics
        if (request.getExtrinsics() != null) {
            String orgId = request.getExtrinsics().get("OrgId");
            String userId = request.getExtrinsics().get("UserId");
            
            if (orgId == null) {
                log.warn("Oracle: OrgId extrinsic missing (may be required for E-Business Suite)");
            }
            
            if (userId == null) {
                log.warn("Oracle: UserId extrinsic missing (recommended)");
            }
        }
        
        log.debug("Oracle validation passed");
    }
}
