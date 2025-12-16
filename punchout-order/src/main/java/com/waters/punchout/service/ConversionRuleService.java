package com.waters.punchout.service;

import com.waters.punchout.entity.ConversionRuleDocument;
import com.waters.punchout.repository.ConversionRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionRuleService {
    
    private final ConversionRuleRepository repository;
    
    @Cacheable(value = "conversionRules", key = "#customerId + '_' + #documentType")
    public Optional<ConversionRuleDocument> getActiveRule(String customerId, String documentType) {
        log.info("Loading conversion rule from DB for customer={}, docType={}", customerId, documentType);
        List<ConversionRuleDocument> rules = repository.findByCustomerIdAndDocumentTypeAndActiveTrue(customerId, documentType);
        return rules.stream()
                .max((r1, r2) -> Integer.compare(
                    r1.getPriority() != null ? r1.getPriority() : 0,
                    r2.getPriority() != null ? r2.getPriority() : 0));
    }
    
    @Cacheable(value = "conversionRules", key = "'all_' + #customerId")
    public List<ConversionRuleDocument> getRulesForCustomer(String customerId) {
        return repository.findByCustomerIdAndActiveTrue(customerId);
    }
    
    public List<ConversionRuleDocument> getAllActiveRules() {
        return repository.findByActiveTrue();
    }
    
    public List<ConversionRuleDocument> getAllRules() {
        return repository.findAll();
    }
    
    @CacheEvict(value = "conversionRules", allEntries = true)
    public ConversionRuleDocument saveRule(ConversionRuleDocument rule) {
        if (rule.getId() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }
        rule.setUpdatedAt(LocalDateTime.now());
        log.info("Saving conversion rule for customer={}, docType={}", rule.getCustomerId(), rule.getDocumentType());
        return repository.save(rule);
    }
    
    @CacheEvict(value = "conversionRules", allEntries = true)
    public void deleteRule(String id) {
        repository.deleteById(id);
        log.info("Deleted conversion rule: {}", id);
    }
    
    @CacheEvict(value = "conversionRules", allEntries = true)
    public void refreshCache() {
        log.info("Conversion rules cache cleared");
    }
}
