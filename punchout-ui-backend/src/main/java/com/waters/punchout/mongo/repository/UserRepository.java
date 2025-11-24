package com.waters.punchout.mongo.repository;

import com.waters.punchout.mongo.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDocument, String> {
    
    Optional<UserDocument> findByUserId(String userId);
    
    Optional<UserDocument> findByUsername(String username);
    
    Optional<UserDocument> findByEmail(String email);
    
    List<UserDocument> findByRole(String role);
    
    List<UserDocument> findByStatus(String status);
    
    List<UserDocument> findByDepartment(String department);
    
    @Query("{ 'role': ?0, 'status': ?1 }")
    List<UserDocument> findByRoleAndStatus(String role, String status);
    
    @Query("{ '$or': [ { 'username': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'firstName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'lastName': { '$regex': ?0, '$options': 'i' } } ] }")
    List<UserDocument> searchUsers(String searchTerm);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    long countByStatus(String status);
    
    long countByRole(String role);
}
