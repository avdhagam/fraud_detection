package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private String documentId;
    private boolean isValid;
    private double finalRiskScore;
    private String riskLevel; // High, Medium, Low
    private String decision; // Approve, Review, Reject
    private String nextSteps; // Recommended actions
    private String remarks; // Additional processing comments

    private Map<String, Object> ocrResults;
    private Map<String, Object> qualityResults;
    private Map<String, Object> forgeryResults;
    private Map<String, Object> validationResults;

}
