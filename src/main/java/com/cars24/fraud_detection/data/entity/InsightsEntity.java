package com.cars24.fraud_detection.data.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InsightsEntity {
    private String doctype;
    private String status;
    private String documentName;
    private Double score;
    private String description;
    private LocalDateTime uploadedAt;

}
