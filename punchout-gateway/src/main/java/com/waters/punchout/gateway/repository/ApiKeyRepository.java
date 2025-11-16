package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyValue(String keyValue);
    List<ApiKey> findByCustomerName(String customerName);
    List<ApiKey> findByEnvironment(String environment);
    List<ApiKey> findByEnabled(Boolean enabled);
    List<ApiKey> findByCustomerNameAndEnvironment(String customerName, String environment);
}
