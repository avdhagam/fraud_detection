package com.cars24.fraud_detection.data.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InsightsEntity {
    private String leadId; // Add leadId field
    private String id;
    private String doctype;
    private String documentName;
    private String status;
    private Double score;
    private String description;
    private LocalDateTime uploadedAt;
}