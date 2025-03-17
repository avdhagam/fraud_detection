package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.utils.FileUtils;
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

    @Value("${document.storage.path}")
    private String storagePath;

    // Constructor injection with Qualifier
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

            // Run workflow (OCR, Validation, Quality, Forgery Detection)
            DocumentResponse response = workflowInitiator.processDocument(request);
            log.debug("Workflow execution completed for file: {}, Result: {}", request.getFileName(), response);

            DocumentEntity entity = FileUtils.buildDocumentEntity(request, response);
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
//        return documentDao.getDocumentById(documentId)
//                .map(DocumentEntity::toResponse)
//                .orElseThrow(() -> {
//                    log.warn("Document not found for ID: {}", documentId);
//                    return new DocumentProcessingException("Document not found for ID: " + documentId);
//                });
    }
}


