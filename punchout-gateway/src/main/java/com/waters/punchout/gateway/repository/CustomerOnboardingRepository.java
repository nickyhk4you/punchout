package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.CustomerOnboarding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOnboardingRepository extends MongoRepository<CustomerOnboarding, String> {
    
    List<CustomerOnboarding> findByCustomerName(String customerName);
    
    List<CustomerOnboarding> findByEnvironment(String environment);
    
    List<CustomerOnboarding> findByCustomerType(String customerType);
    
    Optional<CustomerOnboarding> findByCustomerNameAndEnvironment(String customerName, String environment);
    
    List<CustomerOnboarding> findByDeployedTrue();
    
    List<CustomerOnboarding> findByStatus(String status);
}
