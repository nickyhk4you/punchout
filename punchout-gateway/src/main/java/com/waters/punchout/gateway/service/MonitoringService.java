package com.waters.punchout.gateway.service;

import com.waters.punchout.gateway.entity.NetworkRequestDocument;
import com.waters.punchout.gateway.entity.PunchOutSessionDocument;
import com.waters.punchout.gateway.entity.OrderDocument;
import com.waters.punchout.gateway.repository.NetworkRequestRepository;
import com.waters.punchout.gateway.repository.PunchOutSessionRepository;
import com.waters.punchout.gateway.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonitoringService {

    private final NetworkRequestRepository networkRequestRepository;
    private final PunchOutSessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    public MonitoringService(
            NetworkRequestRepository networkRequestRepository,
            PunchOutSessionRepository sessionRepository,
            OrderRepository orderRepository,
            MongoTemplate mongoTemplate) {
        this.networkRequestRepository = networkRequestRepository;
        this.sessionRepository = sessionRepository;
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<NetworkRequestDocument> getNetworkRequests(String direction, String requestType, Integer limit) {
        Query query = new Query();
        
        if (direction != null && !direction.isEmpty()) {
            query.addCriteria(Criteria.where("direction").is(direction));
        }
        
        if (requestType != null && !requestType.isEmpty()) {
            query.addCriteria(Criteria.where("requestType").is(requestType));
        }
        
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        
        if (limit != null && limit > 0) {
            query.limit(limit);
        } else {
            query.limit(100);
        }
        
        return mongoTemplate.find(query, NetworkRequestDocument.class);
    }

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Total counts
        long totalSessions = sessionRepository.count();
        long totalOrders = orderRepository.count();
        long totalRequests = networkRequestRepository.count();
        
        metrics.put("totalSessions", totalSessions);
        metrics.put("totalOrders", totalOrders);
        metrics.put("totalRequests", totalRequests);
        
        // Recent sessions (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Query recentSessionsQuery = new Query(Criteria.where("timestamp").gte(last24Hours));
        long recentSessions = mongoTemplate.count(recentSessionsQuery, PunchOutSessionDocument.class);
        metrics.put("recentSessions24h", recentSessions);
        
        // Recent orders (last 24 hours)
        Query recentOrdersQuery = new Query(Criteria.where("timestamp").gte(last24Hours));
        long recentOrders = mongoTemplate.count(recentOrdersQuery, OrderDocument.class);
        metrics.put("recentOrders24h", recentOrders);
        
        return metrics;
    }

    public Map<String, Object> getRequestMetrics(Integer hours) {
        Map<String, Object> metrics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        Query query = new Query(Criteria.where("timestamp").gte(since));
        List<NetworkRequestDocument> requests = mongoTemplate.find(query, NetworkRequestDocument.class);
        
        // Total requests
        metrics.put("totalRequests", requests.size());
        
        // Success vs failure
        long successCount = requests.stream().filter(r -> Boolean.TRUE.equals(r.getSuccess())).count();
        long failureCount = requests.size() - successCount;
        metrics.put("successCount", successCount);
        metrics.put("failureCount", failureCount);
        metrics.put("successRate", requests.isEmpty() ? 0 : (successCount * 100.0 / requests.size()));
        
        // By direction
        Map<String, Long> byDirection = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDirection() != null ? r.getDirection() : "UNKNOWN",
                        Collectors.counting()
                ));
        metrics.put("byDirection", byDirection);
        
        // By request type
        Map<String, Long> byType = requests.stream()
                .filter(r -> r.getRequestType() != null)
                .collect(Collectors.groupingBy(
                        NetworkRequestDocument::getRequestType,
                        Collectors.counting()
                ));
        metrics.put("byType", byType);
        
        // Average response time
        OptionalDouble avgDuration = requests.stream()
                .filter(r -> r.getDuration() != null)
                .mapToLong(NetworkRequestDocument::getDuration)
                .average();
        metrics.put("avgResponseTime", avgDuration.isPresent() ? avgDuration.getAsDouble() : 0);
        
        // Requests over time (hourly breakdown)
        Map<String, Long> requestsOverTime = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTimestamp().toLocalDate().toString() + " " + r.getTimestamp().getHour() + ":00",
                        Collectors.counting()
                ));
        metrics.put("requestsOverTime", requestsOverTime);
        
        return metrics;
    }

    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check MongoDB connection
            mongoTemplate.getDb().getName();
            health.put("mongodb", Map.of("status", "UP", "message", "Connected"));
        } catch (Exception e) {
            health.put("mongodb", Map.of("status", "DOWN", "message", e.getMessage()));
        }
        
        // Check recent activity
        LocalDateTime last5Minutes = LocalDateTime.now().minusMinutes(5);
        Query recentQuery = new Query(Criteria.where("timestamp").gte(last5Minutes));
        long recentActivity = mongoTemplate.count(recentQuery, NetworkRequestDocument.class);
        
        health.put("recentActivity", Map.of(
                "last5Minutes", recentActivity,
                "status", recentActivity > 0 ? "ACTIVE" : "IDLE"
        ));
        
        health.put("timestamp", LocalDateTime.now());
        health.put("overallStatus", "UP");
        
        return health;
    }

    public List<Map<String, Object>> getRecentLogs(Integer limit, String level, String service) {
        // For now, we'll simulate logs from network requests
        // In a real implementation, you'd integrate with actual log aggregation
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.limit(limit);
        
        List<NetworkRequestDocument> requests = mongoTemplate.find(query, NetworkRequestDocument.class);
        
        return requests.stream()
                .map(req -> {
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("timestamp", req.getTimestamp());
                    logEntry.put("level", Boolean.TRUE.equals(req.getSuccess()) ? "INFO" : "ERROR");
                    logEntry.put("service", "gateway");
                    logEntry.put("message", String.format("%s %s request to %s - %s", 
                            req.getDirection(),
                            req.getRequestType(),
                            req.getDestination() != null ? req.getDestination() : req.getUrl(),
                            Boolean.TRUE.equals(req.getSuccess()) ? "SUCCESS" : "FAILED"
                    ));
                    logEntry.put("sessionKey", req.getSessionKey());
                    logEntry.put("requestId", req.getRequestId());
                    logEntry.put("statusCode", req.getStatusCode());
                    logEntry.put("duration", req.getDuration());
                    return logEntry;
                })
                .filter(log -> level == null || level.isEmpty() || log.get("level").equals(level.toUpperCase()))
                .collect(Collectors.toList());
    }
}
