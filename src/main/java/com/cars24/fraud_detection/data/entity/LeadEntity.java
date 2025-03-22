package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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

    private String adharNumber;
    private String panNumber;

    // Ground Truth Data for Validation (Data Entered by Agent)
    private String verifiedName;
    private String verifiedDob;
    private String verifiedGender;
    private String verifiedAdhar;
    private String verifiedPan;

    // Optional: Additional Lead Information
    private String address;
    private String phoneNumber;

    @CreatedDate
    private LocalDateTime createdAt;
}