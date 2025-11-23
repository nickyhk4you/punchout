package com.waters.punchout.gateway.health;

import com.waters.punchout.gateway.service.EnvironmentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Health check for external services (Auth Service, Mule Service).
 * Reports detailed status for each dependency.
 */
@Component
@Slf4j
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final WebClient webClient;
    private final EnvironmentConfigService environmentConfigService;
    
    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5);

    public ExternalServiceHealthIndicator(
            WebClient.Builder webClientBuilder,
            EnvironmentConfigService environmentConfigService
    ) {
        this.webClient = webClientBuilder.build();
        this.environmentConfigService = environmentConfigService;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up();
        boolean allHealthy = true;

        // Check auth service for dev environment
        String authServiceStatus = checkAuthService();
        healthBuilder.withDetail("authService", authServiceStatus);
        if (!"reachable".equals(authServiceStatus)) {
            allHealthy = false;
        }

        // Check mule service for dev environment
        String muleServiceStatus = checkMuleService();
        healthBuilder.withDetail("muleService", muleServiceStatus);
        if (!"reachable".equals(muleServiceStatus)) {
            allHealthy = false;
        }

        // Check MongoDB
        String mongoStatus = checkMongoDB();
        healthBuilder.withDetail("mongodb", mongoStatus);
        if (!"reachable".equals(mongoStatus)) {
            allHealthy = false;
        }

        return allHealthy ? healthBuilder.build() : healthBuilder.down().build();
    }

    private String checkAuthService() {
        try {
            String authUrl = environmentConfigService.getAuthServiceUrl("dev");
            if (authUrl == null) {
                return "not_configured";
            }
            
            // Try to reach the service (doesn't need to be a valid endpoint, just check connectivity)
            webClient.get()
                    .uri(authUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(HEALTH_CHECK_TIMEOUT)
                    .block();
            
            return "reachable";
        } catch (Exception e) {
            log.warn("Auth service health check failed: {}", e.getMessage());
            return "unreachable: " + e.getMessage();
        }
    }

    private String checkMuleService() {
        try {
            String muleUrl = environmentConfigService.getMuleServiceUrl("dev");
            if (muleUrl == null) {
                return "not_configured";
            }
            
            // Try to reach the service
            webClient.get()
                    .uri(muleUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(HEALTH_CHECK_TIMEOUT)
                    .block();
            
            return "reachable";
        } catch (Exception e) {
            log.warn("Mule service health check failed: {}", e.getMessage());
            return "unreachable: " + e.getMessage();
        }
    }

    private String checkMongoDB() {
        try {
            // Try to get environment config (this will fail if MongoDB is down)
            environmentConfigService.getConfig("dev");
            return "reachable";
        } catch (Exception e) {
            log.warn("MongoDB health check failed: {}", e.getMessage());
            return "unreachable: " + e.getMessage();
        }
    }
}
