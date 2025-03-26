package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "leads")
public class LeadEntity {

    @Id
    private String id = UUID.randomUUID().toString(); // Generate UUID on creation

    private String agentId; // Reference to Agent (Foreign Key)

    private String name;
    private String email;
    private String dob;  // Date of Birth
    private String gender;
    private String fatherName;

    private String adharNumber;
    private String panNumber;

//    private String docType;

    private String referenceName;
    private String relationToSubject;
    private String subjectOccupation;


    // Optional: Additional Lead Information
//    private String address;
    private String subjectAddress;
    private String phoneNumber;

    private List<AudioEntity> referenceCalls;
    private List<DocumentEntity> aadhaar;
    private List<DocumentEntity> pan;

    @CreatedDate
    private LocalDateTime createdAt;


}