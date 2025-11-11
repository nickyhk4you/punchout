package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.InvoiceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceMongoRepository extends MongoRepository<InvoiceDocument, String> {
    
    Optional<InvoiceDocument> findByInvoiceNumber(String invoiceNumber);
    
    List<InvoiceDocument> findByOrderId(String orderId);
    
    List<InvoiceDocument> findByStatus(String status);
    
    List<InvoiceDocument> findByEnvironment(String environment);
    
    List<InvoiceDocument> findByCustomerId(String customerId);
    
    List<InvoiceDocument> findAllByOrderByReceivedDateDesc();
}
