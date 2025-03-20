package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByUserId(String userReportId);
    List<DocumentEntity> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

}
