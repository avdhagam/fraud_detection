package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id;  // MongoDB uses String IDs by default

    private String extractedData;
    private Double validationScore;
    private Double imageQualityScore;
    private Double forgeryScore;
    private String finalRisk;
}
