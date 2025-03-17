package com.cars24.fraud_detection.data.entity;

import com.cars24.fraud_detection.data.response.DocumentResponse;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public DocumentResponse toResponse() {
        return new DocumentResponse(
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
}
