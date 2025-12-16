package com.waters.punchout.controller;

import com.waters.punchout.entity.ConversionRuleDocument;
import com.waters.punchout.service.ConversionRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversion-rules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConversionRuleController {
    
    private final ConversionRuleService ruleService;
    
    /**
     * Get all conversion rules
     */
    @GetMapping
    public ResponseEntity<List<ConversionRuleDocument>> getAllRules() {
        log.info("GET /api/conversion-rules");
        return ResponseEntity.ok(ruleService.getAllRules());
    }
    
    /**
     * Get active rules only
     */
    @GetMapping("/active")
    public ResponseEntity<List<ConversionRuleDocument>> getActiveRules() {
        log.info("GET /api/conversion-rules/active");
        return ResponseEntity.ok(ruleService.getAllActiveRules());
    }
    
    /**
     * Get rules for a specific customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ConversionRuleDocument>> getRulesForCustomer(@PathVariable String customerId) {
        log.info("GET /api/conversion-rules/customer/{}", customerId);
        return ResponseEntity.ok(ruleService.getRulesForCustomer(customerId));
    }
    
    /**
     * Get specific rule for customer and document type
     */
    @GetMapping("/customer/{customerId}/{documentType}")
    public ResponseEntity<ConversionRuleDocument> getRule(
            @PathVariable String customerId,
            @PathVariable String documentType) {
        log.info("GET /api/conversion-rules/customer/{}/{}", customerId, documentType);
        return ruleService.getActiveRule(customerId, documentType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new conversion rule
     */
    @PostMapping
    public ResponseEntity<ConversionRuleDocument> createRule(@RequestBody ConversionRuleDocument rule) {
        log.info("POST /api/conversion-rules - creating rule for customer={}, docType={}", 
            rule.getCustomerId(), rule.getDocumentType());
        rule.setId(null); // Ensure new ID is generated
        ConversionRuleDocument saved = ruleService.saveRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    /**
     * Update an existing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConversionRuleDocument> updateRule(
            @PathVariable String id,
            @RequestBody ConversionRuleDocument rule) {
        log.info("PUT /api/conversion-rules/{}", id);
        rule.setId(id);
        ConversionRuleDocument saved = ruleService.saveRule(rule);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Delete a rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        log.info("DELETE /api/conversion-rules/{}", id);
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Clear the conversion rules cache (force reload from DB)
     */
    @PostMapping("/cache/refresh")
    public ResponseEntity<Map<String, String>> refreshCache() {
        log.info("POST /api/conversion-rules/cache/refresh");
        ruleService.refreshCache();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Conversion rules cache cleared. Rules will be reloaded on next request."
        ));
    }
    
    /**
     * Duplicate an existing rule for a new customer
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ConversionRuleDocument> duplicateRule(
            @PathVariable String id,
            @RequestParam String newCustomerId) {
        log.info("POST /api/conversion-rules/{}/duplicate for customer={}", id, newCustomerId);
        
        return ruleService.getActiveRule(id, null)
                .or(() -> ruleService.getAllRules().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst())
                .map(original -> {
                    ConversionRuleDocument copy = new ConversionRuleDocument();
                    copy.setCustomerId(newCustomerId);
                    copy.setDocumentType(original.getDocumentType());
                    copy.setVersion("1.0");
                    copy.setActive(false); // Start inactive
                    copy.setPriority(original.getPriority());
                    copy.setFieldMappings(original.getFieldMappings());
                    copy.setDefaultValues(original.getDefaultValues());
                    copy.setTransformations(original.getTransformations());
                    copy.setDescription("Duplicated from " + original.getCustomerId());
                    
                    return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ruleService.saveRule(copy));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
