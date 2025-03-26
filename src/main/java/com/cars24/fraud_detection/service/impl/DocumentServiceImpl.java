package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentDao documentDao;
    private final LeadDao leadDao;
    private final WorkflowInitiator workflowInitiator;

    @Value("${document.storage.path}")
    private String storagePath;

    private final DocumentTypeConfig documentTypeConfig;

    public DocumentServiceImpl(DocumentDao documentDao, LeadDao leadDao,
                               @Qualifier("documentWorkflow") WorkflowInitiator workflowInitiator,
                               DocumentTypeConfig documentTypeConfig) { // Include docTypeConfig
        this.documentDao = documentDao;
        this.leadDao = leadDao;
        this.workflowInitiator = workflowInitiator;
        this.documentTypeConfig = documentTypeConfig; // Include docTypeConfig
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {

        try {
            // 1. Validate Lead Existence
            LeadEntity lead = leadDao.findLeadById(request.getLeadId())
                    .orElseThrow(() -> new DocumentProcessingException("Lead not found with ID: " + request.getLeadId()));

            // 2.  Generate unique file path
            String archivePath = findDocumentPath(request.getFileName());

            // 3. Run workflow (OCR, Validation, Quality, Forgery Detection)
            DocumentResponse response = workflowInitiator.processDocument(request);
            log.debug("Workflow execution completed for file: {}, Result: {}", request.getFileName(), response);

            // 4. Create and save the DocumentEntity
            DocumentEntity documentEntity = DocumentEntity.builder()
                    .leadId(request.getLeadId())
                    .agentId(request.getAgentId())
                    .documentType(request.getDocumentType())
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

            DocumentEntity savedDocument = documentDao.saveDocument(documentEntity);

            // 5. Map saved entity to DTO and return
            return savedDocument.toResponse();

        } catch (Exception e) {
            log.error("Error processing document for lead {}: {}", request.getLeadId(), e.getMessage(), e);
            throw new DocumentProcessingException("Failed to process document: " + e.getMessage());
        }
    }

    @Override
    public DocumentResponse getDocumentById(String documentId) {
        DocumentEntity entity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Document not found for ID: " + documentId));

        return entity.toResponse();
    }

    @Override
    public DocumentEntity findDocumentEntityById(String documentId) {
        return documentDao.getDocumentById(documentId).orElse(null); // Return null if not found
    }
    @Override
    public List<DocumentEntity> getDocumentsByLeadId(String leadId) {
        return documentDao.findByLeadId(leadId);
    }

    @Override
    public List<String> getRecentDocumentNames(String leadId, int limit) {
        log.info("Fetching last {} documents for lead ID: {}", limit, leadId);

        List<DocumentEntity> recentDocs = documentDao.getRecentDocumentsByLeadId(leadId, limit);

        log.info("Total documents fetched: {}", recentDocs.size());
        for (DocumentEntity doc : recentDocs) {
            log.info("Found Document: {} with timestamp: {}", doc.getFileName(), doc.getTimestamp());
        }

        return recentDocs.stream()
                .map(DocumentEntity::getFileName) // Extract only the file names
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DocumentEntity> getDocumentByLeadIdAndType(String leadId, String documentType) {
        List<DocumentEntity> documents = documentDao.findByLeadIdAndDocumentType(leadId, documentType);
        //return documents.stream().findFirst();
        return documents.isEmpty() ? Optional.empty() : Optional.of(documents.get(documents.size() - 1));
    }

    @Override
    public InsightsEntity getDocumentInsights(String documentId) {
        DocumentEntity documentEntity = documentDao.getDocumentById(documentId)
                .orElseThrow(() -> new DocumentProcessingException("Document not found for ID: " + documentId));

        String documentName = documentTypeConfig.getMapping().get(documentEntity.getDocumentType());

        return InsightsEntity.builder()
                .leadId(documentEntity.getLeadId())
                .doctype(documentEntity.getDocumentType())
                .documentName(documentName)
                .status(documentEntity.getStatus())
                .score(documentEntity.getFinalRiskScore())
                .description(documentEntity.getDecision())
                .uploadedAt(documentEntity.getTimestamp())
                .build();
    }


    private String findDocumentPath(String fileName) {
        String archiveDir = "src/main/resources/document_storage/archive";
        File folder = new File(archiveDir);
        Optional<File> matchingFile = Arrays.stream(folder.listFiles())
                .filter(file -> file.getName().endsWith(fileName))
                .findFirst();

        return matchingFile.map(file -> file.getAbsolutePath()
                        .replace(new File("").getAbsolutePath() + File.separator, ""))
                .orElse(null);
    }
    public Optional<DocumentEntity> getDocumentByIdAndType(String documentId, String documentType) {
        return documentDao.findByIdAndDocumentType(documentId, documentType);
    }


}