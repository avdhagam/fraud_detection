package com.cars24.fraud_detection.data.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LeadRequest {

    @NotBlank(message = "Agent ID is required")
    private String agentId; // The ID of the agent creating the lead

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    private String dob;
    private String gender;
    private String adharNumber;
    private String panNumber;

    // Ground Truth Data for Validation
    private String verifiedName;
    private String verifiedDob;
    private String verifiedGender;
    private String verifiedAdhar;
    private String verifiedPan;

    // Optional: Additional Lead Information
    private String address;
    private String phoneNumber;
}