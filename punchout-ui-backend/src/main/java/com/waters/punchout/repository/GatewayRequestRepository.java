package com.waters.punchout.repository;

import com.waters.punchout.entity.GatewayRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatewayRequestRepository extends JpaRepository<GatewayRequest, Long> {
    
    List<GatewayRequest> findBySessionKey(String sessionKey);
    
    List<GatewayRequest> findBySessionKeyOrderByDatetimeDesc(String sessionKey);
}
