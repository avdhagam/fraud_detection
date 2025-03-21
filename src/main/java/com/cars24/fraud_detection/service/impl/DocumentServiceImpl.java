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
import org.springframework.web.multipart.MultipartFile;
import com.cars24.fraud_detection.repository.DocumentRepository;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

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
            //  log.info("Starting document processing for user: {}, File: {}", request.getUserId(), request.getFileName());

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

            // Check if an entry with the same userReportId and documentType exists
            // Optional<DocumentEntity> existingDocument = documentDao.findByUserReportIdAndDocumentType(request.getUserReportId(), request.getDocumentType());
            Optional<DocumentEntity> existingDocument = documentDao.findFirstByUserIdAndDocumentType(
                    request.getUserReportId(), request.getDocumentType()
            );


            if (existingDocument.isPresent()) {
                // Update existing entry
                DocumentEntity entity = existingDocument.get();

                //log.info("Existing document found, updating record: {}", entity);

                entity.setFileName(request.getFileName());
                entity.setFilePath(archivePath);
                entity.setOcrResults(response.getOcrResults());
                entity.setQualityResults(response.getQualityResults());
                entity.setForgeryResults(response.getForgeryResults());
                entity.setValidationResults(response.getValidationResults());
                entity.setFinalRiskScore(response.getFinalRiskScore());
                entity.setRiskLevel(response.getRiskLevel());
                entity.setDecision(response.getDecision());
                entity.setNextSteps(response.getNextSteps());
                entity.setStatus(response.isValid() ? "COMPLETED" : "FAILED");
                entity.setRemarks(response.getRemarks());

                documentDao.updateDocument(entity);
                //  log.info("Updated existing document record: {}", entity);
            } else {
                // Insert new entry
                DocumentEntity newDocumentEntity = DocumentEntity.builder()
                        .userId(request.getUserReportId()) // Original user report ID
                        .documentId(response.getDocumentId()) // Generated document ID
                        .documentType(response.getDocumentType())
                        .fileName(request.getFileName())
                        .filePath(archivePath)
                        .ocrResults(response.getOcrResults())
                        .qualityResults(response.getQualityResults())
                        .forgeryResults(response.getForgeryResults())
                        .validationResults(response.getValidationResults())
                        .finalRiskScore(response.getFinalRiskScore())
                        .riskLevel(response.getRiskLevel())
                        .decision(response.getDecision())
                        .nextSteps(response.getNextSteps())
                        .status(response.isValid() ? "COMPLETED" : "FAILED")
                        .remarks(response.getRemarks())
                        .build();

                documentDao.saveDocument(newDocumentEntity);
                // log.info("Saved new document record: {}", newDocumentEntity);
            }

            return response;

        } catch (Exception e) {
            //   log.error("Error processing document for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new DocumentProcessingException("Failed to process document: " + e.getMessage());
        }
    }

    @Override
    public DocumentResponse getDocumentById(String documentId) {
        //  log.info("Fetching document details for ID: {}", documentId);

        DocumentEntity entity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Document not found for ID: " + documentId));

        //  log.debug("Document retrieved successfully: {}", entity);
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
        // log.info("Fetching DocumentEntity by ID: {}", documentId);
        return documentDao.getDocumentById(documentId).orElse(null); // Return null if not found
    }

    @Override
    public List<String> getRecentDocuments(String userId, int limit) {
        log.info("Fetching last {} documents for user ID: {}", limit, userId);

        List<DocumentEntity> recentDocs = documentDao.getRecentDocumentsByUserId(userId, limit);

        log.info("Total documents fetched: {}", recentDocs.size());
        for (DocumentEntity doc : recentDocs) {
            log.info("Found Document: {} with timestamp: {}", doc.getFileName(), doc.getTimestamp());
        }

        return recentDocs.stream()
                .map(DocumentEntity::getFileName) // ✅ Extract only fileName
                .collect(Collectors.toList());
    }

    public Optional<DocumentEntity> getDocumentByUserIdAndType(String userReportId, String documentType) {
        return documentDao.findByUserIdAndDocumentType(userReportId, documentType);
    }
}