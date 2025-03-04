package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.response.DocumentResponseDTO;
import com.cars24.fraud_detection.repository.DocumentRepository;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    private WorkflowInitiator workflowInitiator;

    @Autowired
    private DocumentRepository documentRepository;

    public DocumentResponseDTO processDocument(MultipartFile file) {
        // Validate file format
        if (!isValidFile(file)) {
            throw new InvalidFileException("Only JPG and PNG are allowed.");
        }

        // Trigger Workflow for processing
        Map<String, Object> workflowResults = workflowInitiator.startWorkflow(file);

        // Save results in MongoDB
        DocumentEntity document = new DocumentEntity();
        document.setExtractedData((String) workflowResults.get("ocr"));
        document.setValidationScore((Double) workflowResults.get("validation_score"));
        document.setImageQualityScore((Double) workflowResults.get("image_quality_score"));
        document.setForgeryScore((Double) workflowResults.get("forgery_score"));
        document.setFinalRisk((String) workflowResults.get("final_risk"));

        documentRepository.save(document);

        return new DocumentResponseDTO(document);
    }

    private boolean isValidFile(MultipartFile file) {
        String fileType = file.getContentType();
        return fileType.equals("image/png") || fileType.equals("image/jpeg");
    }
}

