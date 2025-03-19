package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class DocumentResponse {

    private String userReportId;

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

    public DocumentResponse(String userReportId, String documentId, boolean isValid, double finalRiskScore, String riskLevel, String decision, String nextSteps, String remarks, Map<String, Object> ocrResults, Map<String, Object> qualityResults, Map<String, Object> forgeryResults, Map<String, Object> validationResults) {
        this.userReportId = userReportId;
        this.documentId = documentId;
        this.isValid = isValid;
        this.finalRiskScore = finalRiskScore;
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.nextSteps = nextSteps;
        this.remarks = remarks;
        this.ocrResults = ocrResults;
        this.qualityResults = qualityResults;
        this.forgeryResults = forgeryResults;
        this.validationResults = validationResults;
    }
}
