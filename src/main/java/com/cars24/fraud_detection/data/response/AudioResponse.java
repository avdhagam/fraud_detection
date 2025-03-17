// AudioResponse.java
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
    private String uuid;

    private String userReportId;

    private Map<String, Object> llmExtraction;
    private Map<String, Object> audioAnalysis;
    private List<String> transcript;
    private String referenceName;
    private String subjectName;
    private String subjectAddress;
    private String relationToSubject;
    private String subjectOccupation;
    private double overallScore;
    private List<String> explanation;
    private Map<String, Double> fieldByFieldScores;
    private String status;

    public AudioResponse(String id, String userReportId, List<String> transcript, String referenceName, String subjectName, String subjectAddress, String relationToSubject, String subjectOccupation, double overallScore, List<String> explanation, Map<String, Double> fieldByFieldScores, String status) {
    }
}