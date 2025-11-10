package com.waters.punchout.gateway.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.gateway.model.PunchOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CxmlToJsonConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public PunchOutRequest convertCxmlToRequest(String cxmlContent) throws Exception {
        log.debug("Converting cXML to PunchOutRequest");
        
        // Parse XML to JsonNode for easier navigation
        JsonNode rootNode = xmlMapper.readTree(cxmlContent);
        JsonNode requestNode = rootNode.path("Request").path("PunchOutSetupRequest");
        
        PunchOutRequest request = new PunchOutRequest();
        
        // Use BuyerCookie from cXML as sessionKey, or generate if missing
        String buyerCookie = requestNode.path("BuyerCookie").asText();
        if (buyerCookie == null || buyerCookie.isEmpty()) {
            buyerCookie = generateSessionKey();
        }
        request.setSessionKey(buyerCookie);
        request.setOperation(requestNode.path("operation").asText("create"));
        request.setBuyerCookie(buyerCookie);
        request.setTimestamp(LocalDateTime.now());
        
        // Extract contact email
        JsonNode contactNode = requestNode.path("Contact").path("Email");
        if (!contactNode.isMissingNode()) {
            request.setContactEmail(contactNode.asText());
        }
        
        // Extract cart return URL
        JsonNode browserFormPost = requestNode.path("BrowserFormPost").path("URL");
        if (!browserFormPost.isMissingNode()) {
            request.setCartReturnUrl(browserFormPost.asText());
        }
        
        // Extract identity information from Header
        JsonNode headerNode = rootNode.path("Header");
        request.setFromIdentity(extractIdentity(headerNode.path("From")));
        request.setToIdentity(extractIdentity(headerNode.path("To")));
        request.setSenderIdentity(extractIdentity(headerNode.path("Sender")));
        
        log.info("Converted cXML to PunchOutRequest: sessionKey={}", request.getSessionKey());
        return request;
    }
    
    public String convertRequestToJson(PunchOutRequest request) throws Exception {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
    }
    
    public Map<String, Object> convertToThirdPartyPayload(PunchOutRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionKey", request.getSessionKey());
        payload.put("operation", request.getOperation());
        payload.put("buyerCookie", request.getBuyerCookie());
        payload.put("contactEmail", request.getContactEmail());
        payload.put("cartReturnUrl", request.getCartReturnUrl());
        payload.put("timestamp", request.getTimestamp().toString());
        return payload;
    }
    
    private String extractIdentity(JsonNode credentialNode) {
        JsonNode identityNode = credentialNode.path("Credential").path("Identity");
        return identityNode.isMissingNode() ? null : identityNode.asText();
    }
    
    private String generateSessionKey() {
        return "SESSION_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
