package com.cars24.fraud_detection.data.response;

import com.fasterxml.jackson.annotation.JacksonInject;
import lombok.Data;

import java.time.LocalDateTime;
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
    private String docType;
    private String referenceName;
    private String relationToSubject;
    private String subjectOccupation;
    private String address;
    private String phoneNumber;
    private LocalDateTime createdAt;

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
        this.address = leadEntity.getAddress();
        this.phoneNumber = leadEntity.getPhoneNumber();
        this.createdAt = leadEntity.getCreatedAt();
    }
}