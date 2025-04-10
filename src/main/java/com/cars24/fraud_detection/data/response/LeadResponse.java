package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.fasterxml.jackson.annotation.JacksonInject;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.cars24.fraud_detection.data.entity.LeadEntity;

@Data
public class LeadResponse {

    private String id;
    private String agentId;
    private String name;
    private String email;
    private String dob;
    private String gender;
    private String fatherName;
    private String adharNumber;
    private String panNumber;
    private String referenceName;
    private String relationToSubject;
    private String subjectOccupation;
    private String subjectAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;

    private List<AudioEntity> referenceCalls;
    private List<DocumentEntity> aadhaar;
    private List<DocumentEntity> pan;

    // getters and setters

    public LeadResponse(LeadEntity leadEntity) {
        this.id = leadEntity.getId();
        this.agentId = leadEntity.getAgentId();
        this.name = leadEntity.getName();
        this.email = leadEntity.getEmail();
        this.dob = leadEntity.getDob();
        this.gender = leadEntity.getGender();
        this.fatherName = leadEntity.getFatherName();
        this.adharNumber = leadEntity.getAdharNumber();
        this.panNumber = leadEntity.getPanNumber();
        this.referenceName = leadEntity.getReferenceName();
        this.relationToSubject = leadEntity.getRelationToSubject();
        this.subjectOccupation = leadEntity.getSubjectOccupation();
        this.subjectAddress = leadEntity.getSubjectAddress();
        this.phoneNumber = leadEntity.getPhoneNumber();
        this.createdAt = leadEntity.getCreatedAt();
        this.referenceCalls = leadEntity.getReferenceCalls();
        this.aadhaar = leadEntity.getAadhaar();
        this.pan = leadEntity.getPan();
    }
}