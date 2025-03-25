package com.cars24.fraud_detection.data.dao;


import com.cars24.fraud_detection.data.entity.DocumentEntity;

import java.util.List;
import java.util.Optional;


public interface DocumentDao {
    void saveDocument(DocumentEntity document);
    Optional<DocumentEntity> getDocumentById(String documentId);
    void updateDocument(DocumentEntity document);
    Optional<DocumentEntity> findByUserIdAndDocumentType(String userReportId, String documentType);
    Optional<DocumentEntity> findFirstByUserIdAndDocumentType(String userReportId, String documentType);
    List<DocumentEntity> getRecentDocumentsByUserId(String userId, int limit);
}
