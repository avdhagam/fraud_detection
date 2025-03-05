package com.cars24.fraud_detection.service;


import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;

public interface DocumentService {
    DocumentResponse processDocument(DocumentRequest request);
    DocumentResponse getDocumentById(String documentId);
}
