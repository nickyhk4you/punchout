package com.waters.punchout.repository;

import com.waters.punchout.entity.OrderObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderObjectRepository extends JpaRepository<OrderObject, String> {
    
    Optional<OrderObject> findBySessionKey(String sessionKey);
}
