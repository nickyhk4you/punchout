package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.NetworkRequestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkRequestMongoRepository extends MongoRepository<NetworkRequestDocument, String> {
    
    List<NetworkRequestDocument> findBySessionKey(String sessionKey);
    
    List<NetworkRequestDocument> findBySessionKeyOrderByTimestampAsc(String sessionKey);
    
    List<NetworkRequestDocument> findBySessionKeyAndDirection(String sessionKey, String direction);
}
