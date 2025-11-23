package com.waters.punchout.gateway.controller;

import com.waters.punchout.common.dto.OrderResponse;
import com.waters.punchout.gateway.service.OrderOrchestrationService;
import com.waters.punchout.gateway.service.PunchOutOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/punchout")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PunchOutGatewayController {
    
    private final PunchOutOrchestrationService orchestrationService;
    private final OrderOrchestrationService orderOrchestrationService;
    
    @PostMapping(value = "/setup", 
                 consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE}, 
                 produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handlePunchOutSetup(@RequestBody String cxmlContent) {
        log.info("Received PunchOut setup request, content length: {}", cxmlContent != null ? cxmlContent.length() : 0);
        
        if (cxmlContent == null || cxmlContent.trim().isEmpty()) {
            log.error("Empty cXML content received");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildCxmlErrorResponse("Empty cXML content"));
        }
        
        log.debug("cXML Content preview: {}", cxmlContent.substring(0, Math.min(100, cxmlContent.length())));
        
        try {
            Map<String, Object> result = orchestrationService.processPunchOutRequest(cxmlContent, null);
            
            String sessionKey = (String) result.get("sessionKey");
            String catalogUrl = (String) result.get("catalogUrl");
            
            // Build cXML response
            String cxmlResponse = buildCxmlSetupResponse(sessionKey, catalogUrl);
            
            log.info("Returning PunchOut setup response for session: {}", sessionKey);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body(cxmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing PunchOut setup request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildCxmlErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping(value = "/order", 
                 consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handleOrderRequest(@RequestBody String cxmlContent) {
        log.info("Received Order request, content length: {}", cxmlContent != null ? cxmlContent.length() : 0);
        
        try {
            OrderResponse response = orderOrchestrationService.processOrder(cxmlContent);
            
            String cxmlResponse = buildCxmlOrderResponse(response);
            
            log.info("Order processed successfully: orderId={}", response.getOrderId());
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body(cxmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing order request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildCxmlErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "punchout-gateway",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    private String buildCxmlSetupResponse(String sessionKey, String catalogUrl) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<cXML>\n" +
            "  <Response>\n" +
            "    <Status code=\"200\" text=\"success\"/>\n" +
            "    <PunchOutSetupResponse>\n" +
            "      <BuyerCookie>%s</BuyerCookie>\n" +
            "      <StartPage>\n" +
            "        <URL>%s</URL>\n" +
            "      </StartPage>\n" +
            "    </PunchOutSetupResponse>\n" +
            "  </Response>\n" +
            "</cXML>",
            sessionKey,
            catalogUrl
        );
    }
    
    private String buildCxmlSuccessResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<cXML>\n" +
               "  <Response>\n" +
               "    <Status code=\"200\" text=\"success\"/>\n" +
               "  </Response>\n" +
               "</cXML>";
    }
    
    private String buildCxmlOrderResponse(OrderResponse orderResponse) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<cXML>\n" +
            "  <Response>\n" +
            "    <Status code=\"200\" text=\"success\"/>\n" +
            "    <ConfirmationRequest>\n" +
            "      <ConfirmationHeader>\n" +
            "        <confirmID>%s</confirmID>\n" +
            "        <muleOrderID>%s</muleOrderID>\n" +
            "      </ConfirmationHeader>\n" +
            "    </ConfirmationRequest>\n" +
            "  </Response>\n" +
            "</cXML>",
            orderResponse.getOrderId(),
            orderResponse.getMuleOrderId()
        );
    }
    
    private String buildCxmlErrorResponse(String errorMessage) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<cXML>\n" +
            "  <Response>\n" +
            "    <Status code=\"500\" text=\"error\">%s</Status>\n" +
            "  </Response>\n" +
            "</cXML>",
            errorMessage
        );
    }
}
