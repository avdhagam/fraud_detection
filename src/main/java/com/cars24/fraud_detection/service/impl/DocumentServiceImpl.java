package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.cars24.fraud_detection.workflow.impl.WorkflowInitiatorImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final WorkflowInitiatorImpl workflowInitiator;
    private final DocumentDao documentDao;

    public DocumentResponse processDocument(DocumentRequest request) {
        try {
            log.info("Processing document: {}", request.getFileName());

            DocumentResponse response = workflowInitiator.processDocument(request);

            if (response.getExtractedData().isEmpty() || response.getFraudRiskScore() == 0.0) {
                throw new DocumentProcessingException("Document processing failed due to missing or invalid data.");
            }

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
            log.info("Document successfully processed and stored.");
            return response;

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
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
