package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.PunchOutTestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PunchOutTestMongoRepository extends MongoRepository<PunchOutTestDocument, String> {
    
    List<PunchOutTestDocument> findByCatalogRouteIdOrderByTestDateDesc(String catalogRouteId);
    
    List<PunchOutTestDocument> findByTesterOrderByTestDateDesc(String tester);
    
    List<PunchOutTestDocument> findByStatusOrderByTestDateDesc(String status);
    
    List<PunchOutTestDocument> findAllByOrderByTestDateDesc();
}
