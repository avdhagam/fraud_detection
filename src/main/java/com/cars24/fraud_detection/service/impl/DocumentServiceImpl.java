package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
//@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDao documentDao;

    @Qualifier("documentWorkflow") // Specify which bean to use
    private final WorkflowInitiator workflowInitiator;

    private final DocumentTypeConfig documentTypeConfig;

    @Value("${document.storage.path}")
    private String storagePath;

    // Constructor injection with Qualifier
    @Autowired
    public DocumentServiceImpl(DocumentDao documentDao,
                               @Qualifier("documentWorkflow") WorkflowInitiator workflowInitiator,DocumentTypeConfig documentTypeConfig){
        this.documentDao = documentDao;
        this.workflowInitiator = workflowInitiator;
        this.documentTypeConfig = documentTypeConfig;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        try {
            log.info("Available Document Types: {}", documentTypeConfig.getMapping());

            log.info("Starting document processing for user: {}, File: {}", request.getUserId(), request.getFileName());
            // Run workflow (OCR, Validation, Quality, Forgery Detection)
            DocumentResponse response = workflowInitiator.processDocument(request);
            log.debug("Workflow execution completed for file: {}, Result: {}", request.getFileName(), response);

            // Save document details to the database
            DocumentEntity entity = DocumentEntity.builder()
                    .userId(request.getUserId())
                    .fileName(request.getFileName())
                    .status(response.isValid() ? "COMPLETED" : "FAILED")
                    .remarks(response.getRemarks())
                    .ocrResults(response.getOcrResults())
                    .qualityResults(response.getQualityResults())
                    .forgeryResults(response.getForgeryResults())
                    .validationResults(response.getValidationResults())
                    .finalRiskScore(response.getFinalRiskScore())
                    .riskLevel(response.getRiskLevel())
                    .decision(response.getDecision())
                    .nextSteps(response.getNextSteps())
                    .build();

            documentDao.saveDocument(entity);
            log.info("Document saved successfully in database: {} (Status: {})", request.getFileName(), entity.getStatus());

            return response;

        } catch (Exception e) {
            log.error("Error processing document for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new DocumentProcessingException("Failed to process document: " + e.getMessage());
        }
    }

    @Override
    public DocumentResponse getDocumentById(String documentId) {
        log.info("Fetching document details for ID: {}", documentId);

        Optional<DocumentEntity> documentEntityOpt = documentDao.getDocumentById(documentId);

        if (documentEntityOpt.isEmpty()) {
            log.warn("Document not found for ID: {}", documentId);
            throw new DocumentProcessingException("Document not found for ID: " + documentId);
        }

        DocumentResponse response = documentEntityOpt.get().toResponse();
        log.debug("Document retrieved successfully: {}", response);

        return response;
    }
}