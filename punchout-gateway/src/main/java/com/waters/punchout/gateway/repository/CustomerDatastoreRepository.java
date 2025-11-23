package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.CustomerDatastore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDatastoreRepository extends MongoRepository<CustomerDatastore, String> {
    
    List<CustomerDatastore> findByCustomer(String customer);
    
    List<CustomerDatastore> findByEnvironment(String environment);
    
    Optional<CustomerDatastore> findByCustomerAndEnvironment(String customer, String environment);
    
    List<CustomerDatastore> findByEnabledTrue();
    
    List<CustomerDatastore> findByCustomerAndEnabledTrue(String customer);
}
