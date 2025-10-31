package com.waters.punchout.repository;

import com.waters.punchout.entity.PunchOutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PunchOutSessionRepository extends JpaRepository<PunchOutSession, String>, JpaSpecificationExecutor<PunchOutSession> {
    
    Optional<PunchOutSession> findBySessionKey(String sessionKey);
    
    List<PunchOutSession> findByOperation(String operation);
    
    List<PunchOutSession> findByRouteName(String routeName);
    
    List<PunchOutSession> findByEnvironment(String environment);
    
    List<PunchOutSession> findBySessionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM PunchOutSession p WHERE " +
           "(:operation IS NULL OR p.operation = :operation) AND " +
           "(:routeName IS NULL OR p.routeName = :routeName) AND " +
           "(:environment IS NULL OR p.environment = :environment) AND " +
           "(:startDate IS NULL OR p.sessionDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.sessionDate <= :endDate)")
    List<PunchOutSession> findByFilters(
        @Param("operation") String operation,
        @Param("routeName") String routeName,
        @Param("environment") String environment,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
