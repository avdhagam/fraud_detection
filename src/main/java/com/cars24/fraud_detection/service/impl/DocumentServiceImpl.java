package com.cars24.fraud_detection.service.impl;

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

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDao documentDao;
    private final WorkflowInitiator workflowInitiator;

    @Value("${document.storage.path}")
    private String storagePath;

    @Autowired
    public DocumentServiceImpl(DocumentDao documentDao,
                               @Qualifier("documentWorkflow") WorkflowInitiator workflowInitiator) {
        this.documentDao = documentDao;
        this.workflowInitiator = workflowInitiator;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {

        try {
            log.info("Starting document processing for user: {}, File: {}", request.getUserId(), request.getFileName());

            String archivePath = findDocumentPath(request.getFileName());
            // Generate unique file path
           // String filePath = storagePath + "archive" + request.getFileName();
           // request.transferTo(new File(filePath)); // Save file

            // Create DocumentRequest from MultipartFile
            //DocumentRequest request = new DocumentRequest();
            // Keep original user report ID
           // request.setFileName(file.getOriginalFilename());

            // Run workflow (OCR, Validation, Quality, Forgery Detection)
            DocumentResponse response = workflowInitiator.processDocument(request);
            log.debug("Workflow execution completed for file: {}, Result: {}", request.getFileName(), response);

            // Save document details to the database
            DocumentEntity entity = DocumentEntity.builder()
                    .userId(request.getUserReportId()) // Generate document ID
                    .documentId(response.getDocumentId()) // Keep user-provided report ID
                    .fileName(request.getFileName())
                    .filePath(archivePath)
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

        DocumentEntity entity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Document not found for ID: " + documentId));

        log.debug("Document retrieved successfully: {}", entity);
        return entity.toResponse();
    }

    public String findDocumentPath(String fileName) {
        String archiveDir = "src/main/resources/document_storage/archive";
        File folder = new File(archiveDir);
        Optional<File> matchingFile = Arrays.stream(folder.listFiles())
                .filter(file -> file.getName().endsWith(fileName)) // Match the filename
                .findFirst();

        return matchingFile.map(file -> file.getAbsolutePath()
                        .replace(new File("").getAbsolutePath() + File.separator, "")) // ✅ Convert to relative path
                .orElse(null);
    }

    @Override
    public DocumentEntity findDocumentEntityById(String documentId) {
        log.info("Fetching DocumentEntity by ID: {}", documentId);
        return documentDao.getDocumentById(documentId).orElse(null); // Return null if not found
    }
}
