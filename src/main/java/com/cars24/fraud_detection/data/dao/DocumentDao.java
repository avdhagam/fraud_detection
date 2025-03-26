package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import java.util.List;
import java.util.Optional;

public interface DocumentDao {
    DocumentEntity saveDocument(DocumentEntity document);
    DocumentEntity updateDocument(DocumentEntity document);
    Optional<DocumentEntity> getDocumentById(String documentId);
    List<DocumentEntity> findByLeadId(String leadId);
    List<DocumentEntity> getRecentDocumentsByLeadId(String leadId, int limit);
    List<DocumentEntity> findByLeadIdAndDocumentType(String leadId, String documentType);
}