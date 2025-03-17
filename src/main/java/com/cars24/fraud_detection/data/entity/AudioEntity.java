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
    private Map<String, Object> llmExtraction;

    private String userReportId;

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
        return new AudioResponse(id, userReportId, transcript, referenceName, subjectName, subjectAddress, relationToSubject, subjectOccupation, overallScore, explanation, fieldByFieldScores,status);
    }


    public AudioEntity(){}


    public AudioEntity(String userReportId, List<String> transcript, String referenceName, String subjectName,
                       String subjectAddress, String relationToSubject, String subjectOccupation,
                       double overallScore, List<String> explanation, Map<String, Double> fieldByFieldScores, String status) {
        this.userReportId = userReportId;
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

