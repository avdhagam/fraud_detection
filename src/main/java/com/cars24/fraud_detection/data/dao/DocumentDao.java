package com.cars24.fraud_detection.data.dao;


import com.cars24.fraud_detection.data.entity.DocumentEntity;

import java.util.List;
import java.util.Optional;


public interface DocumentDao {
    void saveDocument(DocumentEntity document);
    Optional<DocumentEntity> getDocumentById(String documentId);

    List<DocumentEntity> getRecentDocumentsByUserId(String userId, int limit);
}
