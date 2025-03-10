package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {
    private String documentId;
    private boolean isValid;
    private Map<String, Object> extractedData; // OCR & Validation results
    private double fraudRiskScore;
    private Map<String, Double> individualScores; // Quality, Forgery, etc.
    private String remarks;
}
