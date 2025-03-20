package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByUserId(String userReportId);
    Optional<DocumentEntity> findByUserIdAndDocumentType(String userReportId, String documentType);
    List<DocumentEntity> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
    Optional<DocumentEntity> findFirstByUserIdAndDocumentType(String userReportId, String documentType);
}
