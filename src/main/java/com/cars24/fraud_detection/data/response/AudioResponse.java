package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioResponse {
    private String id; // AudioEntity ID
    private String leadId; // Lead ID
    private String documentType;

    private Map<String, Object> llmExtraction;
    private List<String> transcript;
    private String referenceName;
    private String subjectName;
    private String subjectAddress;
    private String relationToSubject;
    private String subjectOccupation;
    private double overallScore;
    private List<String> explanation;
    private Map<String, Double> fieldByFieldScores;
    private Map<String, Object> audioAnalysis;
    private String status;
}