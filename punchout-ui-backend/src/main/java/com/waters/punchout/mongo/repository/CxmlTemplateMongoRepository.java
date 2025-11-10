package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.CxmlTemplateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CxmlTemplateMongoRepository extends MongoRepository<CxmlTemplateDocument, String> {
    
    List<CxmlTemplateDocument> findByEnvironment(String environment);
    
    Optional<CxmlTemplateDocument> findByEnvironmentAndCustomerId(String environment, String customerId);
    
    Optional<CxmlTemplateDocument> findByEnvironmentAndIsDefaultTrue(String environment);
    
    List<CxmlTemplateDocument> findByCustomerId(String customerId);
}
