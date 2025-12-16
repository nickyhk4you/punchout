package com.waters.punchout.converter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.waters.punchout.converter.CxmlConverter;
import com.waters.punchout.entity.ConversionRuleDocument;
import com.waters.punchout.entity.ConversionRuleDocument.FieldMapping;
import com.waters.punchout.entity.ConversionRuleDocument.Transformation;
import com.waters.punchout.service.ConversionRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DynamicCxmlConverter implements CxmlConverter {
    
    private final ConversionRuleService ruleService;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();
    
    private String currentCustomerId;
    
    @Override
    public Object convert(String cxmlContent, String documentType) {
        try {
            Optional<ConversionRuleDocument> ruleOpt = ruleService.getActiveRule(currentCustomerId, documentType);
            
            if (ruleOpt.isEmpty()) {
                log.info("No dynamic rule found for customer={}, docType={}, using default conversion", 
                    currentCustomerId, documentType);
                return defaultConversion(cxmlContent);
            }
            
            ConversionRuleDocument rule = ruleOpt.get();
            log.info("Applying dynamic conversion rule: {} v{} for customer={}", 
                rule.getId(), rule.getVersion(), currentCustomerId);
            
            return applyRule(cxmlContent, rule);
            
        } catch (Exception e) {
            log.error("Error in dynamic conversion for customer={}", currentCustomerId, e);
            throw new RuntimeException("Dynamic conversion failed: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> applyRule(String cxmlContent, ConversionRuleDocument rule) throws Exception {
        Document xmlDoc = parseXml(cxmlContent);
        XPath xPath = xPathFactory.newXPath();
        Map<String, Object> result = new HashMap<>();
        
        if (rule.getDefaultValues() != null) {
            result.putAll(rule.getDefaultValues());
        }
        
        if (rule.getFieldMappings() != null) {
            for (FieldMapping mapping : rule.getFieldMappings()) {
                Object value = extractValue(xmlDoc, xPath, mapping);
                if (value != null) {
                    setNestedValue(result, mapping.getTargetJsonPath(), value);
                } else if (mapping.getRequired() != null && mapping.getRequired()) {
                    throw new RuntimeException("Required field missing: " + mapping.getSourceXPath());
                }
            }
        }
        
        if (rule.getTransformations() != null) {
            for (Transformation transform : rule.getTransformations()) {
                applyTransformation(result, transform);
            }
        }
        
        result.put("_conversionRule", rule.getId());
        result.put("_conversionVersion", rule.getVersion());
        result.put("_convertedAt", LocalDateTime.now().toString());
        
        return result;
    }
    
    private Object extractValue(Document xmlDoc, XPath xPath, FieldMapping mapping) {
        try {
            String value = xPath.evaluate(mapping.getSourceXPath(), xmlDoc);
            
            if (value == null || value.isEmpty()) {
                return mapping.getDefaultValue();
            }
            
            if (mapping.getValueMapping() != null && !mapping.getValueMapping().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, String> valueMap = objectMapper.readValue(mapping.getValueMapping(), Map.class);
                if (valueMap.containsKey(value)) {
                    value = valueMap.get(value);
                }
            }
            
            return convertType(value, mapping.getDataType(), mapping.getDateFormat());
            
        } catch (Exception e) {
            log.warn("Failed to extract value for {}: {}", mapping.getSourceXPath(), e.getMessage());
            return mapping.getDefaultValue();
        }
    }
    
    private Object convertType(String value, String dataType, String dateFormat) {
        if (value == null || dataType == null) return value;
        
        switch (dataType.toUpperCase()) {
            case "INTEGER":
                return Integer.parseInt(value);
            case "DOUBLE":
                return Double.parseDouble(value);
            case "BOOLEAN":
                return Boolean.parseBoolean(value);
            case "DATE":
                if (dateFormat != null) {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(dateFormat)).toString();
                }
                return value;
            default:
                return value;
        }
    }
    
    private void applyTransformation(Map<String, Object> data, Transformation transform) {
        Object value = getNestedValue(data, transform.getTargetField());
        if (value == null) return;
        
        String strValue = value.toString();
        Map<String, String> params = transform.getParameters() != null ? transform.getParameters() : new HashMap<>();
        
        switch (transform.getType().toUpperCase()) {
            case "UPPERCASE":
                setNestedValue(data, transform.getTargetField(), strValue.toUpperCase());
                break;
            case "LOWERCASE":
                setNestedValue(data, transform.getTargetField(), strValue.toLowerCase());
                break;
            case "TRIM":
                setNestedValue(data, transform.getTargetField(), strValue.trim());
                break;
            case "REGEX_REPLACE":
                String pattern = params.get("pattern");
                String replacement = params.get("replacement");
                if (pattern != null) {
                    setNestedValue(data, transform.getTargetField(), 
                        strValue.replaceAll(pattern, replacement != null ? replacement : ""));
                }
                break;
            case "CONCAT":
                String prefix = params.getOrDefault("prefix", "");
                String suffix = params.getOrDefault("suffix", "");
                setNestedValue(data, transform.getTargetField(), prefix + strValue + suffix);
                break;
            case "SPLIT":
                String delimiter = params.getOrDefault("delimiter", ",");
                int index = Integer.parseInt(params.getOrDefault("index", "0"));
                String[] parts = strValue.split(Pattern.quote(delimiter));
                if (index < parts.length) {
                    setNestedValue(data, transform.getTargetField(), parts[index]);
                }
                break;
            default:
                break;
        }
    }
    
    private Document parseXml(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }
    
    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> map, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = map;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<>());
        }
        current.put(parts[parts.length - 1], value);
    }
    
    private Object getNestedValue(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
    
    private Map<String, Object> defaultConversion(String cxmlContent) throws Exception {
        JsonNode xmlNode = xmlMapper.readTree(cxmlContent);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = xmlMapper.convertValue(xmlNode, Map.class);
        return result;
    }
    
    @Override
    public boolean supports(String customerId) {
        this.currentCustomerId = customerId;
        return !ruleService.getRulesForCustomer(customerId).isEmpty();
    }
    
    @Override
    public String getCustomerId() {
        return "DYNAMIC";
    }
}
