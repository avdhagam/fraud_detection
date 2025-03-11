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
    private Map<String, Object> ocrResults;
    private Map<String, Object> qualityResults;
    private Map<String, Object> forgeryResults;
    private Map<String, Object> validationResults;
    private double finalRiskScore;


    public DocumentResponse toResponse() {
        return new DocumentResponse(
                id,
                true,  // Assuming document is valid once processed
                Map.of(
                        "ocrResults", ocrResults,
                        "qualityResults", qualityResults,
                        "forgeryResults", forgeryResults,
                        "validationResults", validationResults
                ),
                finalRiskScore,
                Map.of(
                        "qualityScore", qualityResults != null ? (Double) qualityResults.getOrDefault("score", 0.0) : 0.0,
                        "forgeryScore", forgeryResults != null ? (Double) forgeryResults.getOrDefault("score", 0.0) : 0.0,
                        "validationScore", validationResults != null ? (Double) validationResults.getOrDefault("score", 0.0) : 0.0
                ),
                status  // Assuming status can be used as remarks
        );
    }

}

