package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    Optional<DocumentEntity> findById(String documentId);

}
