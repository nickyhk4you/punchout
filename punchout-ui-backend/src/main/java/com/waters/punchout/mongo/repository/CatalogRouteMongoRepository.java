package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.CatalogRouteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRouteMongoRepository extends MongoRepository<CatalogRouteDocument, String> {
    
    List<CatalogRouteDocument> findByActiveTrue();
    
    List<CatalogRouteDocument> findByNetwork(String network);
    
    List<CatalogRouteDocument> findByType(String type);
}
