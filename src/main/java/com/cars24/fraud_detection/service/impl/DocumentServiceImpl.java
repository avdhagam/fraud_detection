package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDao documentDao;
    private final WorkflowInitiator workflowInitiator;

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        try {
            // Step 1: Process document via workflow (OCR, Validation, Quality, Forgery)
            DocumentResponse response = workflowInitiator.processDocument(request);

            // Step 2: Save document details in database
            DocumentEntity entity = DocumentEntity.builder()
                    .userId(request.getUserId())
                    .fileName(request.getFileName())
                    .filePath("stored_documents/" + request.getFileName())
                    .status("COMPLETED")
                    .ocrResults((Map<String, Object>) response.getExtractedData().get("ocrResults"))
                    .qualityResults((Map<String, Object>) response.getExtractedData().get("qualityResults"))
                    .forgeryResults((Map<String, Object>) response.getExtractedData().get("forgeryResults"))
                    .validationResults((Map<String, Object>) response.getExtractedData().get("validationResults"))
                    .finalRiskScore(response.getFraudRiskScore())
                    .build();


            documentDao.saveDocument(entity);
            return response;

        } catch (Exception e) {
            throw new DocumentProcessingException("Error processing document: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentResponse getDocumentById(String documentId) {
        DocumentEntity documentEntity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Document not found"));
        return documentEntity.toResponse();
    }
}
