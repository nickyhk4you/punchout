package com.waters.punchout.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for recording application metrics using Micrometer.
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record auth service request metrics.
     */
    public void recordAuthRequest(String environment, long durationMs, boolean success) {
        Timer.builder("punchout.auth.request")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Auth service request duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("punchout.auth.requests.total")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Total auth service requests")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record mule service request metrics.
     */
    public void recordMuleRequest(String environment, long durationMs, boolean success) {
        Timer.builder("punchout.mule.request")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Mule service request duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("punchout.mule.requests.total")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Total mule service requests")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record punchout session metrics.
     */
    public void recordPunchoutSession(String environment, String operation, boolean success) {
        Counter.builder("punchout.sessions.total")
                .tag("environment", environment)
                .tag("operation", operation)
                .tag("result", success ? "success" : "failure")
                .description("Total punchout sessions")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record cache hit/miss metrics.
     */
    public void recordCacheAccess(String cacheName, boolean hit) {
        Counter.builder("punchout.cache.access")
                .tag("cache", cacheName)
                .tag("result", hit ? "hit" : "miss")
                .description("Cache access statistics")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record circuit breaker state changes.
     */
    public void recordCircuitBreakerState(String serviceName, String state) {
        Counter.builder("punchout.circuitbreaker.state")
                .tag("service", serviceName)
                .tag("state", state)
                .description("Circuit breaker state changes")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record order processing metrics.
     */
    public void recordOrderProcessing(String environment, long durationMs, boolean success) {
        Timer.builder("punchout.order.processing")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Order processing duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("punchout.orders.total")
                .tag("environment", environment)
                .tag("result", success ? "success" : "failure")
                .description("Total orders processed")
                .register(meterRegistry)
                .increment();
    }
}
