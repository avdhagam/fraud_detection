package com.cars24.fraud_detection.data.entity;

import com.cars24.fraud_detection.data.response.DocumentResponse;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id = UUID.randomUUID().toString(); // Generate UUID on creation

    private String leadId; // Reference to Lead (Foreign Key)
    private String agentId; // Reference to Agent (Foreign Key)

    private String documentType;

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
        return DocumentResponse.builder()
                .documentId(this.id) // Use entity's ID
                .leadId(this.leadId)
                .documentType(this.documentType)
                .isValid("COMPLETED".equals(status))
                .finalRiskScore(this.finalRiskScore)
                .riskLevel(this.riskLevel)
                .decision(this.decision)
                .nextSteps(this.nextSteps)
                .remarks(this.remarks)
                .ocrResults(this.ocrResults)
                .qualityResults(this.qualityResults)
                .forgeryResults(this.forgeryResults)
                .validationResults(this.validationResults)
                .build();
    }

    // Remove the old constructor, let Lombok handle it
}