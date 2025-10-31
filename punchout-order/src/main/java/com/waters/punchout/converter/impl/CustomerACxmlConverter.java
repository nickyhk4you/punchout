package com.waters.punchout.converter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.converter.CxmlConverter;
import com.waters.punchout.model.OrderData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CustomerACxmlConverter implements CxmlConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public Object convert(String cxmlContent, String documentType) {
        try {
            JsonNode xmlNode = xmlMapper.readTree(cxmlContent);
            
            if ("ORDER".equalsIgnoreCase(documentType)) {
                return convertOrderWithCustomLogic(xmlNode);
            }
            
            return xmlMapper.convertValue(xmlNode, Map.class);
        } catch (Exception e) {
            log.error("Error converting cXML for Customer A", e);
            throw new RuntimeException("Conversion failed for Customer A: " + e.getMessage());
        }
    }

    private OrderData convertOrderWithCustomLogic(JsonNode xmlNode) {
        OrderData.OrderDataBuilder builder = OrderData.builder();
        
        builder.orderId(getTextValue(xmlNode, "PurchaseOrderID"));
        builder.customerId("CUSTOMER_A");
        builder.orderDate(getTextValue(xmlNode, "Date"));
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("customerSpecificField", getTextValue(xmlNode, "CustomField"));
        additionalData.put("processingType", "EXPRESS");
        builder.additionalData(additionalData);
        
        builder.items(new ArrayList<>());
        
        return builder.build();
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() ? null : fieldNode.asText();
    }

    @Override
    public boolean supports(String customerId) {
        return "CUSTOMER_A".equalsIgnoreCase(customerId);
    }

    @Override
    public String getCustomerId() {
        return "CUSTOMER_A";
    }
}
