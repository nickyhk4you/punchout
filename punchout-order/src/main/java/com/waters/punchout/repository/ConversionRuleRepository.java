package com.waters.punchout.repository;

import com.waters.punchout.entity.ConversionRuleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ConversionRuleRepository extends MongoRepository<ConversionRuleDocument, String> {
    List<ConversionRuleDocument> findByCustomerIdAndActiveTrue(String customerId);
    List<ConversionRuleDocument> findByCustomerIdAndDocumentTypeAndActiveTrue(String customerId, String documentType);
    Optional<ConversionRuleDocument> findByCustomerIdAndDocumentTypeAndActiveTrueOrderByPriorityDesc(String customerId, String documentType);
    List<ConversionRuleDocument> findByActiveTrue();
    boolean existsByCustomerIdAndDocumentType(String customerId, String documentType);
}
