package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.PunchOutSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PunchOutSessionRepository extends MongoRepository<PunchOutSessionDocument, String> {
    Optional<PunchOutSessionDocument> findBySessionKey(String sessionKey);
}
