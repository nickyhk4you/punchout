package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.CustomerDatastore;
import com.waters.punchout.gateway.repository.CustomerDatastoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDatastoreService {
    
    private final CustomerDatastoreRepository repository;
    
    public List<CustomerDatastore> getAllDatastores() {
        log.info("Fetching all customer datastores");
        return repository.findAll();
    }
    
    public Optional<CustomerDatastore> getDatastoreById(String id) {
        log.info("Fetching datastore by id: {}", id);
        return repository.findById(id);
    }
    
    public List<CustomerDatastore> getDatastoresByCustomer(String customer) {
        log.info("Fetching datastores for customer: {}", customer);
        return repository.findByCustomer(customer);
    }
    
    public List<CustomerDatastore> getDatastoresByEnvironment(String environment) {
        log.info("Fetching datastores for environment: {}", environment);
        return repository.findByEnvironment(environment);
    }
    
    public Optional<CustomerDatastore> getDatastoreByCustomerAndEnvironment(String customer, String environment) {
        log.info("Fetching datastore for customer: {} and environment: {}", customer, environment);
        return repository.findByCustomerAndEnvironment(customer, environment);
    }
    
    public String getValue(String customer, String environment, String key) {
        log.info("Getting value for customer: {}, environment: {}, key: {}", customer, environment, key);
        return repository.findByCustomerAndEnvironment(customer, environment)
                .map(CustomerDatastore::getKeyValuePairs)
                .map(kvp -> kvp.get(key))
                .orElse(null);
    }
    
    public CustomerDatastore createDatastore(CustomerDatastore datastore) {
        log.info("Creating new datastore for customer: {} and environment: {}", 
                datastore.getCustomer(), datastore.getEnvironment());
        
        datastore.setCreatedAt(LocalDateTime.now());
        datastore.setUpdatedAt(LocalDateTime.now());
        
        if (datastore.getEnabled() == null) {
            datastore.setEnabled(true);
        }
        
        return repository.save(datastore);
    }
    
    public CustomerDatastore updateDatastore(String id, CustomerDatastore updatedDatastore) {
        log.info("Updating datastore with id: {}", id);
        
        return repository.findById(id)
                .map(existing -> {
                    existing.setCustomer(updatedDatastore.getCustomer());
                    existing.setEnvironment(updatedDatastore.getEnvironment());
                    existing.setKeyValuePairs(updatedDatastore.getKeyValuePairs());
                    existing.setDescription(updatedDatastore.getDescription());
                    existing.setEnabled(updatedDatastore.getEnabled());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setUpdatedBy(updatedDatastore.getUpdatedBy());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Datastore not found with id: " + id));
    }
    
    public void deleteDatastore(String id) {
        log.info("Deleting datastore with id: {}", id);
        repository.deleteById(id);
    }
    
    public CustomerDatastore addOrUpdateKeyValue(String id, String key, String value) {
        log.info("Adding/Updating key-value for datastore id: {}, key: {}", id, key);
        
        return repository.findById(id)
                .map(datastore -> {
                    Map<String, String> kvp = datastore.getKeyValuePairs();
                    kvp.put(key, value);
                    datastore.setKeyValuePairs(kvp);
                    datastore.setUpdatedAt(LocalDateTime.now());
                    return repository.save(datastore);
                })
                .orElseThrow(() -> new RuntimeException("Datastore not found with id: " + id));
    }
    
    public CustomerDatastore removeKey(String id, String key) {
        log.info("Removing key from datastore id: {}, key: {}", id, key);
        
        return repository.findById(id)
                .map(datastore -> {
                    Map<String, String> kvp = datastore.getKeyValuePairs();
                    kvp.remove(key);
                    datastore.setKeyValuePairs(kvp);
                    datastore.setUpdatedAt(LocalDateTime.now());
                    return repository.save(datastore);
                })
                .orElseThrow(() -> new RuntimeException("Datastore not found with id: " + id));
    }
}
