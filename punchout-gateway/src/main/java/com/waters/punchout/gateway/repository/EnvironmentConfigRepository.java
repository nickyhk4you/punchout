package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.EnvironmentConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentConfigRepository extends MongoRepository<EnvironmentConfig, String> {
    
    Optional<EnvironmentConfig> findByEnvironmentAndEnabledTrue(String environment);
    
    Optional<EnvironmentConfig> findByEnvironment(String environment);
}
