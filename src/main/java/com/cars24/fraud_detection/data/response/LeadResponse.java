package com.cars24.fraud_detection.data.response;

import lombok.Data;

@Data
public class LeadResponse {

    private String id;
    private String agentId;
    private String name;
    private String email;
    private String dob;
    private String gender;
    private String adharNumber;
    private String panNumber;

    // Ground Truth Data
    private String verifiedName;
    private String verifiedDob;
    private String verifiedGender;
    private String verifiedAdhar;
    private String verifiedPan;

    private String address;
    private String phoneNumber;
}