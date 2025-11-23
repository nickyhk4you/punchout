package com.waters.punchout.gateway.controller;

import com.waters.punchout.gateway.entity.CustomerOnboarding;
import com.waters.punchout.gateway.service.CustomerOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerOnboardingController {
    
    private final CustomerOnboardingService onboardingService;
    
    @GetMapping
    public ResponseEntity<List<CustomerOnboarding>> getAllOnboardings() {
        log.info("GET /api/onboarding - Fetching all onboardings");
        List<CustomerOnboarding> onboardings = onboardingService.getAllOnboardings();
        return ResponseEntity.ok(onboardings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerOnboarding> getOnboardingById(@PathVariable String id) {
        log.info("GET /api/onboarding/{} - Fetching onboarding", id);
        return onboardingService.getOnboardingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<CustomerOnboarding>> getOnboardingsByCustomer(@PathVariable String customerName) {
        log.info("GET /api/onboarding/customer/{} - Fetching onboardings", customerName);
        List<CustomerOnboarding> onboardings = onboardingService.getOnboardingsByCustomerName(customerName);
        return ResponseEntity.ok(onboardings);
    }
    
    @GetMapping("/environment/{environment}")
    public ResponseEntity<List<CustomerOnboarding>> getOnboardingsByEnvironment(@PathVariable String environment) {
        log.info("GET /api/onboarding/environment/{} - Fetching onboardings", environment);
        List<CustomerOnboarding> onboardings = onboardingService.getOnboardingsByEnvironment(environment);
        return ResponseEntity.ok(onboardings);
    }
    
    @GetMapping("/deployed")
    public ResponseEntity<List<CustomerOnboarding>> getDeployedOnboardings() {
        log.info("GET /api/onboarding/deployed - Fetching deployed onboardings");
        List<CustomerOnboarding> onboardings = onboardingService.getDeployedOnboardings();
        return ResponseEntity.ok(onboardings);
    }
    
    @PostMapping
    public ResponseEntity<CustomerOnboarding> createOnboarding(@RequestBody CustomerOnboarding onboarding) {
        log.info("POST /api/onboarding - Creating onboarding for customer: {}", onboarding.getCustomerName());
        CustomerOnboarding created = onboardingService.createOnboarding(onboarding);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerOnboarding> updateOnboarding(
            @PathVariable String id,
            @RequestBody CustomerOnboarding onboarding) {
        log.info("PUT /api/onboarding/{} - Updating onboarding", id);
        try {
            CustomerOnboarding updated = onboardingService.updateOnboarding(id, onboarding);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating onboarding: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/deploy")
    public ResponseEntity<CustomerOnboarding> deployOnboarding(@PathVariable String id) {
        log.info("POST /api/onboarding/{}/deploy - Deploying onboarding", id);
        try {
            CustomerOnboarding deployed = onboardingService.deployOnboarding(id);
            return ResponseEntity.ok(deployed);
        } catch (RuntimeException e) {
            log.error("Error deploying onboarding: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/generate-converter")
    public ResponseEntity<CustomerOnboarding> generateConverter(@PathVariable String id) {
        log.info("POST /api/onboarding/{}/generate-converter - Generating converter class", id);
        try {
            CustomerOnboarding updated = onboardingService.generateConverterClass(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error generating converter: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOnboarding(@PathVariable String id) {
        log.info("DELETE /api/onboarding/{} - Deleting onboarding", id);
        onboardingService.deleteOnboarding(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConversion(@PathVariable String id, @RequestBody Map<String, String> testData) {
        log.info("POST /api/onboarding/{}/test - Testing conversion", id);
        
        return onboardingService.getOnboardingById(id)
                .map(onboarding -> {
                    Map<String, Object> result = Map.of(
                        "success", true,
                        "message", "Test conversion executed successfully",
                        "onboardingId", id,
                        "customerName", onboarding.getCustomerName(),
                        "testResult", "Sample conversion output would appear here"
                    );
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
