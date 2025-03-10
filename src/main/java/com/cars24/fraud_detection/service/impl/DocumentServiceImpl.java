package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.utils.FileUtils;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDao documentDao;
    private final WorkflowInitiator workflowInitiator;

    @Value("${document.storage.path}")
    private String storagePath;

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        try {
            log.info("Processing document for user: {}", request.getUserId());

            // Validate file type (JPG/PNG check)
            if (!FileUtils.isValidFileType(request.getFileName())) {
                log.warn("Invalid file type: {}. Only JPG and PNG are allowed.", request.getFileName());
                throw new DocumentProcessingException("Unsupported file type. Only JPG and PNG are allowed.");
            }

            //Run workflow (OCR, Validation, Quality, Forgery Detection)
            DocumentResponse response = workflowInitiator.processDocument(request);
            log.info("Workflow execution completed for file: {}", request.getFileName());

            //Save document details to database
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

        }  catch (IllegalArgumentException e) {
            log.warn("Invalid document request: {}", e.getMessage());
            throw new DocumentProcessingException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to process document. Please try again.");
        }
    }

    @Override
    public DocumentResponse getDocumentById(String documentId) {
        log.info("Fetching document by ID: {}", documentId);

        DocumentEntity documentEntity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> {
                    log.warn("Document not found: {}", documentId);
                    return new DocumentProcessingException("Document not found");
                });

        log.info("Document fetched successfully: {}", documentId);
        return documentEntity.toResponse();
    }
}
