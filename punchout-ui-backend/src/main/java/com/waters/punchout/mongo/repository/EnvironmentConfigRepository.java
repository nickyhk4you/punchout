package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.EnvironmentConfigDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentConfigRepository extends MongoRepository<EnvironmentConfigDocument, String> {
    
    Optional<EnvironmentConfigDocument> findByEnvironment(String environment);
    
    Optional<EnvironmentConfigDocument> findByEnvironmentAndEnabledTrue(String environment);
    
    List<EnvironmentConfigDocument> findByEnabledTrue();
    
    boolean existsByEnvironment(String environment);
    
    void deleteByEnvironment(String environment);
}
