package com.waters.punchout.gateway.controller;

import com.waters.punchout.gateway.entity.NetworkRequestDocument;
import com.waters.punchout.gateway.service.MonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@Slf4j
@CrossOrigin(origins = "*")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/network-requests")
    public ResponseEntity<List<NetworkRequestDocument>> getAllNetworkRequests(
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) Integer limit) {
        log.info("GET /api/monitoring/network-requests - direction: {}, requestType: {}, limit: {}", 
                direction, requestType, limit);
        
        List<NetworkRequestDocument> requests = monitoringService.getNetworkRequests(direction, requestType, limit);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        log.info("GET /api/monitoring/metrics - Fetching system metrics");
        Map<String, Object> metrics = monitoringService.getSystemMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/requests")
    public ResponseEntity<Map<String, Object>> getRequestMetrics(
            @RequestParam(required = false, defaultValue = "24") Integer hours) {
        log.info("GET /api/monitoring/metrics/requests - hours: {}", hours);
        Map<String, Object> metrics = monitoringService.getRequestMetrics(hours);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        log.info("GET /api/monitoring/health - Fetching health status");
        Map<String, Object> health = monitoringService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/logs/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentLogs(
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service) {
        log.info("GET /api/monitoring/logs/recent - limit: {}, level: {}, service: {}", limit, level, service);
        List<Map<String, Object>> logs = monitoringService.getRecentLogs(limit, level, service);
        return ResponseEntity.ok(logs);
    }
}
