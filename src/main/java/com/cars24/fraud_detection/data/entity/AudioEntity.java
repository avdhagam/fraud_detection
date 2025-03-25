package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "audio_entities")
@Data
public class AudioEntity {

    @Id
    private String id;
    private String documentType;

    private Map<String, Object> llmExtraction;
    private String userId;
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

    @CreatedDate
    private LocalDateTime timestamp;
}