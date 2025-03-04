package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public abstract class DocumentRepository implements MongoRepository<DocumentEntity, String> {
}
