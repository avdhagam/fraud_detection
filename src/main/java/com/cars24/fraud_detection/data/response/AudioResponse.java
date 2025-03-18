// AudioResponse.java
package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

<<<<<<< HEAD
    public AudioResponse(String id, List<String> transcript, String referenceName, String subjectName, String subjectAddress, String relationToSubject, String subjectOccupation, double overallScore, List<String> explanation, Map<String, Double> fieldByFieldScores,String status) {
    }
=======
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4
}