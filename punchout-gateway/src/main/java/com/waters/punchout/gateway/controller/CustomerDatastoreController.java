package com.waters.punchout.gateway.controller;

import com.waters.punchout.gateway.entity.CustomerDatastore;
import com.waters.punchout.gateway.service.CustomerDatastoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datastore")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerDatastoreController {
    
    private final CustomerDatastoreService datastoreService;
    
    @GetMapping
    public ResponseEntity<List<CustomerDatastore>> getAllDatastores() {
        log.info("GET /api/datastore - Fetching all datastores");
        List<CustomerDatastore> datastores = datastoreService.getAllDatastores();
        return ResponseEntity.ok(datastores);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDatastore> getDatastoreById(@PathVariable String id) {
        log.info("GET /api/datastore/{} - Fetching datastore", id);
        return datastoreService.getDatastoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customer}")
    public ResponseEntity<List<CustomerDatastore>> getDatastoresByCustomer(@PathVariable String customer) {
        log.info("GET /api/datastore/customer/{} - Fetching datastores", customer);
        List<CustomerDatastore> datastores = datastoreService.getDatastoresByCustomer(customer);
        return ResponseEntity.ok(datastores);
    }
    
    @GetMapping("/environment/{environment}")
    public ResponseEntity<List<CustomerDatastore>> getDatastoresByEnvironment(@PathVariable String environment) {
        log.info("GET /api/datastore/environment/{} - Fetching datastores", environment);
        List<CustomerDatastore> datastores = datastoreService.getDatastoresByEnvironment(environment);
        return ResponseEntity.ok(datastores);
    }
    
    @GetMapping("/customer/{customer}/environment/{environment}")
    public ResponseEntity<CustomerDatastore> getDatastoreByCustomerAndEnvironment(
            @PathVariable String customer,
            @PathVariable String environment) {
        log.info("GET /api/datastore/customer/{}/environment/{} - Fetching datastore", customer, environment);
        return datastoreService.getDatastoreByCustomerAndEnvironment(customer, environment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customer}/environment/{environment}/key/{key}")
    public ResponseEntity<Map<String, String>> getValue(
            @PathVariable String customer,
            @PathVariable String environment,
            @PathVariable String key) {
        log.info("GET /api/datastore/customer/{}/environment/{}/key/{} - Fetching value", customer, environment, key);
        String value = datastoreService.getValue(customer, environment, key);
        if (value != null) {
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<CustomerDatastore> createDatastore(@RequestBody CustomerDatastore datastore) {
        log.info("POST /api/datastore - Creating datastore for customer: {} and environment: {}", 
                datastore.getCustomer(), datastore.getEnvironment());
        CustomerDatastore created = datastoreService.createDatastore(datastore);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDatastore> updateDatastore(
            @PathVariable String id,
            @RequestBody CustomerDatastore datastore) {
        log.info("PUT /api/datastore/{} - Updating datastore", id);
        try {
            CustomerDatastore updated = datastoreService.updateDatastore(id, datastore);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating datastore: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatastore(@PathVariable String id) {
        log.info("DELETE /api/datastore/{} - Deleting datastore", id);
        datastoreService.deleteDatastore(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/key/{key}")
    public ResponseEntity<CustomerDatastore> addOrUpdateKeyValue(
            @PathVariable String id,
            @PathVariable String key,
            @RequestBody Map<String, String> payload) {
        log.info("PUT /api/datastore/{}/key/{} - Adding/Updating key-value", id, key);
        try {
            String value = payload.get("value");
            CustomerDatastore updated = datastoreService.addOrUpdateKeyValue(id, key, value);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error adding/updating key-value: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}/key/{key}")
    public ResponseEntity<CustomerDatastore> removeKey(
            @PathVariable String id,
            @PathVariable String key) {
        log.info("DELETE /api/datastore/{}/key/{} - Removing key", id, key);
        try {
            CustomerDatastore updated = datastoreService.removeKey(id, key);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error removing key: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
