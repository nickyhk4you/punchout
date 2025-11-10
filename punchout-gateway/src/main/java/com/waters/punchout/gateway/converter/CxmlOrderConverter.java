package com.waters.punchout.gateway.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.gateway.entity.OrderAddress;
import com.waters.punchout.gateway.entity.OrderDocument;
import com.waters.punchout.gateway.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CxmlOrderConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    
    public OrderDocument convertCxmlToOrder(String cxmlContent) throws Exception {
        log.debug("Converting cXML to OrderDocument");
        
        JsonNode root = xmlMapper.readTree(cxmlContent);
        JsonNode orderNode = root.path("Request").path("OrderRequest");
        JsonNode headerNode = orderNode.path("OrderRequestHeader");
        
        OrderDocument order = new OrderDocument();
        
        order.setOrderId(headerNode.path("orderID").asText());
        order.setOrderDate(parseDateTime(headerNode.path("orderDate").asText()));
        order.setOrderType(headerNode.path("type").asText("regular"));
        order.setOrderVersion(headerNode.path("orderVersion").asText("1"));
        
        JsonNode totalNode = headerNode.path("Total").path("Money");
        order.setTotal(parseMoney(totalNode));
        order.setCurrency(totalNode.path("currency").asText("USD"));
        
        JsonNode taxNode = headerNode.path("Tax").path("Money");
        if (!taxNode.isMissingNode()) {
            order.setTaxAmount(parseMoney(taxNode));
        }
        
        order.setShipTo(extractAddress(headerNode.path("ShipTo").path("Address")));
        order.setBillTo(extractAddress(headerNode.path("BillTo").path("Address")));
        
        order.setItems(extractItems(orderNode));
        
        order.setExtrinsics(extractExtrinsics(headerNode));
        
        order.setComments(extractComments(headerNode));
        
        order.setStatus("RECEIVED");
        order.setReceivedAt(LocalDateTime.now());
        
        log.info("Converted cXML to order: orderId={}, items={}, total={}", 
                order.getOrderId(), order.getItems().size(), order.getTotal());
        
        return order;
    }
    
    private OrderAddress extractAddress(JsonNode addressNode) {
        if (addressNode.isMissingNode()) {
            return null;
        }
        
        OrderAddress address = new OrderAddress();
        address.setAddressId(addressNode.path("addressID").asText());
        address.setName(extractName(addressNode));
        address.setDeliverTo(addressNode.path("DeliverTo").asText());
        
        JsonNode postalAddress = addressNode.path("PostalAddress");
        if (!postalAddress.isMissingNode()) {
            address.setStreet(extractStreet(postalAddress));
            address.setCity(postalAddress.path("City").asText());
            address.setState(postalAddress.path("State").asText());
            address.setPostalCode(postalAddress.path("PostalCode").asText());
            address.setCountry(postalAddress.path("Country").path("isoCountryCode").asText());
        }
        
        address.setEmail(addressNode.path("Email").asText());
        
        JsonNode phoneNode = addressNode.path("Phone");
        if (!phoneNode.isMissingNode()) {
            address.setPhone(phoneNode.path("TelephoneNumber").path("Number").asText());
        }
        
        return address;
    }
    
    private String extractName(JsonNode addressNode) {
        JsonNode nameNode = addressNode.path("Name");
        if (nameNode.isTextual()) {
            return nameNode.asText();
        }
        return nameNode.asText();
    }
    
    private String extractStreet(JsonNode postalAddress) {
        JsonNode streetNode = postalAddress.path("Street");
        if (streetNode.isArray()) {
            StringBuilder street = new StringBuilder();
            streetNode.forEach(node -> {
                if (street.length() > 0) street.append(", ");
                street.append(node.asText());
            });
            return street.toString();
        }
        return streetNode.asText();
    }
    
    private List<OrderItem> extractItems(JsonNode orderNode) {
        List<OrderItem> items = new ArrayList<>();
        
        JsonNode itemsNode = orderNode.path("ItemOut");
        if (itemsNode.isArray()) {
            itemsNode.forEach(itemNode -> items.add(extractItem(itemNode)));
        } else if (!itemsNode.isMissingNode()) {
            items.add(extractItem(itemsNode));
        }
        
        return items;
    }
    
    private OrderItem extractItem(JsonNode itemNode) {
        OrderItem item = new OrderItem();
        
        item.setLineNumber(itemNode.path("lineNumber").asInt());
        item.setQuantity(itemNode.path("quantity").asInt());
        
        JsonNode itemId = itemNode.path("ItemID");
        item.setSupplierPartId(itemId.path("SupplierPartID").asText());
        item.setSupplierPartAuxiliaryId(itemId.path("SupplierPartAuxiliaryID").asText());
        
        JsonNode detail = itemNode.path("ItemDetail");
        JsonNode descNode = detail.path("Description");
        if (descNode.isTextual()) {
            item.setDescription(descNode.asText());
        } else {
            item.setDescription(descNode.path("ShortName").asText());
        }
        
        item.setUnitOfMeasure(detail.path("UnitOfMeasure").asText());
        
        JsonNode classificationNode = detail.path("Classification");
        if (!classificationNode.isMissingNode()) {
            item.setUnspsc(classificationNode.path("domain").asText());
        }
        
        JsonNode unitPriceNode = detail.path("UnitPrice").path("Money");
        item.setUnitPrice(parseMoney(unitPriceNode));
        item.setCurrency(unitPriceNode.path("currency").asText("USD"));
        
        if (item.getUnitPrice() != null && item.getQuantity() != null) {
            item.setExtendedAmount(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        item.setExtrinsics(extractExtrinsics(detail));
        
        return item;
    }
    
    private Map<String, String> extractExtrinsics(JsonNode node) {
        Map<String, String> extrinsics = new HashMap<>();
        
        JsonNode extrinsicsNode = node.path("Extrinsic");
        if (extrinsicsNode.isArray()) {
            extrinsicsNode.forEach(ext -> {
                String name = ext.path("name").asText();
                String value = ext.asText();
                if (name != null && !name.isEmpty()) {
                    extrinsics.put(name, value);
                }
            });
        } else if (!extrinsicsNode.isMissingNode()) {
            String name = extrinsicsNode.path("name").asText();
            String value = extrinsicsNode.asText();
            if (name != null && !name.isEmpty()) {
                extrinsics.put(name, value);
            }
        }
        
        return extrinsics;
    }
    
    private String extractComments(JsonNode headerNode) {
        JsonNode commentsNode = headerNode.path("Comments");
        if (commentsNode.isTextual()) {
            return commentsNode.asText();
        }
        return commentsNode.asText();
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}, using current time", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    private BigDecimal parseMoney(JsonNode moneyNode) {
        if (moneyNode.isMissingNode()) {
            return BigDecimal.ZERO;
        }
        
        String value = moneyNode.asText("0");
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            log.warn("Failed to parse money value: {}", value);
            return BigDecimal.ZERO;
        }
    }
    
    public Map<String, Object> convertOrderToJson(OrderDocument order) {
        Map<String, Object> json = new HashMap<>();
        
        json.put("orderId", order.getOrderId());
        json.put("orderDate", order.getOrderDate().toString());
        json.put("orderType", order.getOrderType());
        json.put("total", order.getTotal());
        json.put("currency", order.getCurrency());
        
        if (order.getShipTo() != null) {
            json.put("shipTo", convertAddressToMap(order.getShipTo()));
        }
        
        if (order.getBillTo() != null) {
            json.put("billTo", convertAddressToMap(order.getBillTo()));
        }
        
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (order.getItems() != null) {
            order.getItems().forEach(item -> itemsList.add(convertItemToMap(item)));
        }
        json.put("items", itemsList);
        
        if (order.getExtrinsics() != null) {
            json.put("extrinsics", order.getExtrinsics());
        }
        
        return json;
    }
    
    private Map<String, Object> convertAddressToMap(OrderAddress address) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", address.getName());
        map.put("street", address.getStreet());
        map.put("city", address.getCity());
        map.put("state", address.getState());
        map.put("postalCode", address.getPostalCode());
        map.put("country", address.getCountry());
        map.put("email", address.getEmail());
        map.put("phone", address.getPhone());
        return map;
    }
    
    private Map<String, Object> convertItemToMap(OrderItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("lineNumber", item.getLineNumber());
        map.put("quantity", item.getQuantity());
        map.put("supplierPartId", item.getSupplierPartId());
        map.put("description", item.getDescription());
        map.put("unitPrice", item.getUnitPrice());
        map.put("extendedAmount", item.getExtendedAmount());
        map.put("currency", item.getCurrency());
        map.put("unitOfMeasure", item.getUnitOfMeasure());
        if (item.getExtrinsics() != null) {
            map.put("extrinsics", item.getExtrinsics());
        }
        return map;
    }
}
