package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByLeadId(String leadId);
    List<DocumentEntity> findByLeadIdAndDocumentType(String leadId, String documentType);
    List<DocumentEntity> findByLeadIdOrderByTimestampDesc(String leadId, Pageable pageable);
    List<DocumentEntity> findByAgentIdAndLeadId(String agentId,String leadId);
    Optional<DocumentEntity> findByIdAndDocumentType(String documentId, String documentType);
    @Query(value = "{ 'leadId': ?0, 'documentType': { $regex: ?1, $options: 'i' } }", sort = "{ 'timestamp': -1 }")
    List<DocumentEntity> getRecentDocumentsByLeadIdAndType(String leadId, String doctype, Pageable pageable);

}