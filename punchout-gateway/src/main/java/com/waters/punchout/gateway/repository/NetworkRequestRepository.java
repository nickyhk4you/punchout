package com.waters.punchout.gateway.repository;

import com.waters.punchout.gateway.entity.NetworkRequestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkRequestRepository extends MongoRepository<NetworkRequestDocument, String> {
    List<NetworkRequestDocument> findBySessionKey(String sessionKey);
}
