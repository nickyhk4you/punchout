package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.SecurityAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends MongoRepository<SecurityAuditLog, String> {
    List<SecurityAuditLog> findByEventType(String eventType);
    List<SecurityAuditLog> findBySeverity(String severity);
    List<SecurityAuditLog> findByCustomerName(String customerName);
    List<SecurityAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<SecurityAuditLog> findByTimestampAfterOrderByTimestampDesc(LocalDateTime since, Pageable pageable);
}
