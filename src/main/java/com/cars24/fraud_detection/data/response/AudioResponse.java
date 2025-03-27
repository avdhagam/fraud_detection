package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
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

    public <E, K, V> AudioResponse(String uuid, Object o, ArrayList<E> es, Object o1, Object o2, Object o3, Object o4, Object o5, double v, ArrayList<E> es1, HashMap<K,V> kvHashMap, String error) {
        throw new UnsupportedOperationException("Constructor not implemented yet");
    }

//    public AudioResponse(String uuid, String agentId, Map<String, Object> llmExtractionResult, Map<String, Object> audioAnalysisMap, List<String> transcript, String referenceName, String subjectName, String subjectAddress, String relationToSubject, String subjectOccupation, Double overallScore, List<String> explanation, Map<String, Double> fieldByFieldScores, String status) {
//    }
    public AudioResponse(String id, String agentId, Map<String, Object> llmExtractionResult, Map<String, Object> audioAnalysisMap, List<String> transcript, String referenceName, String subjectName, String subjectAddress, String relationToSubject, String subjectOccupation, Double overallScore, List<String> explanation, Map<String, Double> fieldByFieldScores, String status) {
        this.id = id;
        this.leadId = agentId;
        this.llmExtraction = llmExtractionResult;
        this.audioAnalysis = audioAnalysisMap;
        this.transcript = transcript;
        this.referenceName = referenceName;
        this.subjectName = subjectName;
        this.subjectAddress = subjectAddress;
        this.relationToSubject = relationToSubject;
        this.subjectOccupation = subjectOccupation;
        this.overallScore = overallScore;
        this.explanation = explanation;
        this.fieldByFieldScores = fieldByFieldScores;
        this.status = status;
    }
}