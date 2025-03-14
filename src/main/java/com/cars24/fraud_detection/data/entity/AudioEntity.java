package com.cars24.fraud_detection.data.entity;

import com.cars24.fraud_detection.data.response.AudioResponse;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "audio_entities")
@Data
public class AudioEntity {

    @Id
    private String id;
    //private Map<String, Object> llmExtraction;
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

    public AudioResponse toResponse() {
        return new AudioResponse(id, transcript, referenceName, subjectName, subjectAddress, relationToSubject, subjectOccupation, overallScore, explanation, fieldByFieldScores,status);
    }
}