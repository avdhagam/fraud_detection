package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;

import java.util.List;
import java.util.Optional;

public interface DocumentService {
    DocumentResponse processDocument(DocumentRequest request);
    DocumentResponse getDocumentById(String documentId);
    DocumentEntity findDocumentEntityById(String documentId);
    List<DocumentEntity> getDocumentsByLeadId(String leadId);
    List<String> getRecentDocumentNames(String leadId, int limit);
    Optional<DocumentEntity> getDocumentByLeadIdAndType(String leadId, String documentType);
    InsightsEntity getDocumentInsights(String documentId); // Added this line
}