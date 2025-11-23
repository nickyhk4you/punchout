package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.CustomerOnboarding;
import com.waters.punchout.gateway.repository.CustomerOnboardingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOnboardingService {
    
    private final CustomerOnboardingRepository repository;
    
    public List<CustomerOnboarding> getAllOnboardings() {
        log.info("Fetching all customer onboardings");
        return repository.findAll();
    }
    
    public Optional<CustomerOnboarding> getOnboardingById(String id) {
        log.info("Fetching onboarding by id: {}", id);
        return repository.findById(id);
    }
    
    public List<CustomerOnboarding> getOnboardingsByCustomerName(String customerName) {
        log.info("Fetching onboardings for customer: {}", customerName);
        return repository.findByCustomerName(customerName);
    }
    
    public List<CustomerOnboarding> getOnboardingsByEnvironment(String environment) {
        log.info("Fetching onboardings for environment: {}", environment);
        return repository.findByEnvironment(environment);
    }
    
    public List<CustomerOnboarding> getDeployedOnboardings() {
        log.info("Fetching all deployed onboardings");
        return repository.findByDeployedTrue();
    }
    
    public CustomerOnboarding createOnboarding(CustomerOnboarding onboarding) {
        log.info("Creating new onboarding for customer: {}", onboarding.getCustomerName());
        
        onboarding.setCreatedAt(LocalDateTime.now());
        onboarding.setUpdatedAt(LocalDateTime.now());
        
        if (onboarding.getStatus() == null) {
            onboarding.setStatus("DRAFT");
        }
        
        if (onboarding.getDeployed() == null) {
            onboarding.setDeployed(false);
        }
        
        return repository.save(onboarding);
    }
    
    public CustomerOnboarding updateOnboarding(String id, CustomerOnboarding updatedOnboarding) {
        log.info("Updating onboarding with id: {}", id);
        
        return repository.findById(id)
                .map(existing -> {
                    existing.setCustomerName(updatedOnboarding.getCustomerName());
                    existing.setCustomerType(updatedOnboarding.getCustomerType());
                    existing.setNetwork(updatedOnboarding.getNetwork());
                    existing.setEnvironment(updatedOnboarding.getEnvironment());
                    existing.setSampleCxml(updatedOnboarding.getSampleCxml());
                    existing.setTargetJson(updatedOnboarding.getTargetJson());
                    existing.setFieldMappings(updatedOnboarding.getFieldMappings());
                    existing.setNotes(updatedOnboarding.getNotes());
                    existing.setStatus(updatedOnboarding.getStatus());
                    existing.setConverterClass(updatedOnboarding.getConverterClass());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setUpdatedBy(updatedOnboarding.getUpdatedBy());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Onboarding not found with id: " + id));
    }
    
    public CustomerOnboarding deployOnboarding(String id) {
        log.info("Deploying onboarding with id: {}", id);
        
        return repository.findById(id)
                .map(onboarding -> {
                    onboarding.setDeployed(true);
                    onboarding.setDeployedAt(LocalDateTime.now());
                    onboarding.setStatus("DEPLOYED");
                    onboarding.setUpdatedAt(LocalDateTime.now());
                    return repository.save(onboarding);
                })
                .orElseThrow(() -> new RuntimeException("Onboarding not found with id: " + id));
    }
    
    public void deleteOnboarding(String id) {
        log.info("Deleting onboarding with id: {}", id);
        repository.deleteById(id);
    }
    
    public CustomerOnboarding generateConverterClass(String id) {
        log.info("Generating converter class for onboarding id: {}", id);
        
        return repository.findById(id)
                .map(onboarding -> {
                    String converterClassName = generateConverterClassName(onboarding);
                    onboarding.setConverterClass(converterClassName);
                    onboarding.setStatus("READY_TO_DEPLOY");
                    onboarding.setUpdatedAt(LocalDateTime.now());
                    return repository.save(onboarding);
                })
                .orElseThrow(() -> new RuntimeException("Onboarding not found with id: " + id));
    }
    
    private String generateConverterClassName(CustomerOnboarding onboarding) {
        String customerType = onboarding.getCustomerType();
        String customerName = onboarding.getCustomerName().replaceAll("[^a-zA-Z0-9]", "");
        return String.format("%s%sConverter", customerName, customerType);
    }
}
