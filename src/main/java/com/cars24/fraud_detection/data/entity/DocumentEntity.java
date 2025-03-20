package com.cars24.fraud_detection.data.entity;

import com.cars24.fraud_detection.data.response.DocumentResponse;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id;

    private String userId;
    private String documentType;

    private String documentId;
    private String fileName;
    private String filePath;
    private String status;  // PROCESSING, COMPLETED, FAILED
    private String remarks; // Additional comments on processing status

    private double finalRiskScore;
    private String riskLevel;  // LOW, MEDIUM, HIGH
    private String decision;   // Approve, Review, Reject
    private String nextSteps;  // Recommended actions

    // Each verification aspect is separately stored
    private Map<String, Object> ocrResults;
    private Map<String, Object> qualityResults;
    private Map<String, Object> forgeryResults;
    private Map<String, Object> validationResults;

    @CreatedDate
    private LocalDateTime timestamp;

    public DocumentResponse toResponse() {
        return new DocumentResponse(
                userId,
                id,
                "COMPLETED".equals(status),
                finalRiskScore,
                riskLevel,
                decision,
                nextSteps,
                remarks,
                ocrResults,
                qualityResults,
                forgeryResults,
                validationResults
        );
    }

    public DocumentEntity(String userId, String documentId, String fileName, String filePath, String status, String remarks,
                          double finalRiskScore, String riskLevel, String decision, String nextSteps,
                          Map<String, Object> ocrResults, Map<String, Object> qualityResults,
                          Map<String, Object> forgeryResults, Map<String, Object> validationResults) {
        this.userId = userId;
        this.documentId = documentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.remarks = remarks;
        this.finalRiskScore = finalRiskScore;
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.nextSteps = nextSteps;
        this.ocrResults = ocrResults;
        this.qualityResults = qualityResults;
        this.forgeryResults = forgeryResults;
        this.validationResults = validationResults;
    }

}
