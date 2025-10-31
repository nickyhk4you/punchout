package com.waters.punchout.converter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DefaultCxmlConverter implements CxmlConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object convert(String cxmlContent, String documentType) {
        try {
            JsonNode xmlNode = xmlMapper.readTree(cxmlContent);
            
            if ("ORDER".equalsIgnoreCase(documentType)) {
                return convertOrder(xmlNode);
            }
            
            return xmlMapper.convertValue(xmlNode, Map.class);
        } catch (Exception e) {
            log.error("Error converting cXML for default customer", e);
            throw new RuntimeException("Conversion failed: " + e.getMessage());
        }
    }

    private OrderData convertOrder(JsonNode xmlNode) {
        OrderData.OrderDataBuilder builder = OrderData.builder();
        
        builder.orderId(getTextValue(xmlNode, "OrderID"));
        builder.orderDate(getTextValue(xmlNode, "OrderDate"));
        builder.buyerInfo(getTextValue(xmlNode, "BuyerCookie"));
        
        List<OrderData.OrderItem> items = new ArrayList<>();
        JsonNode itemsNode = xmlNode.path("ItemOut");
        if (itemsNode.isArray()) {
            itemsNode.forEach(item -> items.add(convertOrderItem(item)));
        } else if (!itemsNode.isMissingNode()) {
            items.add(convertOrderItem(itemsNode));
        }
        
        builder.items(items);
        builder.additionalData(new HashMap<>());
        
        return builder.build();
    }

    private OrderData.OrderItem convertOrderItem(JsonNode itemNode) {
        return OrderData.OrderItem.builder()
                .lineNumber(getTextValue(itemNode, "lineNumber"))
                .itemId(getTextValue(itemNode, "ItemID"))
                .description(getTextValue(itemNode, "Description"))
                .quantity(getIntValue(itemNode, "Quantity"))
                .unitPrice(getDoubleValue(itemNode, "UnitPrice"))
                .uom(getTextValue(itemNode, "UnitOfMeasure"))
                .build();
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() ? null : fieldNode.asText();
    }

    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() ? null : fieldNode.asInt();
    }

    private Double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() ? null : fieldNode.asDouble();
    }

    @Override
    public boolean supports(String customerId) {
        return "DEFAULT".equalsIgnoreCase(customerId);
    }

    @Override
    public String getCustomerId() {
        return "DEFAULT";
    }
}
