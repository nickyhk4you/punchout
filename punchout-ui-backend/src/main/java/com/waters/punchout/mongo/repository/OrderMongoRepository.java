package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    
    Optional<OrderDocument> findByOrderId(String orderId);
    
    List<OrderDocument> findBySessionKey(String sessionKey);
    
    List<OrderDocument> findByStatus(String status);
    
    List<OrderDocument> findByCustomerId(String customerId);
    
    List<OrderDocument> findByEnvironment(String environment);
}
