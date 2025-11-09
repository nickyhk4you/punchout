package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.PunchOutSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PunchOutSessionMongoRepository extends MongoRepository<PunchOutSessionDocument, String> {
    
    Optional<PunchOutSessionDocument> findBySessionKey(String sessionKey);
}
