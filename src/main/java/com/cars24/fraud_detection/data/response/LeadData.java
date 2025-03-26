package com.cars24.fraud_detection.data.response;

import lombok.Data;

@Data
public class LeadData {
    private String id;
    private String agentId;

    //Reference Call Data
    private Integer numberOfReferenceCalls;
    private Integer numberOfAcceptedReferenceCalls;
    private Double agentTalkTime;
    private Double referenceTalkTime;

    //Aadhar Data
    private Integer numberOfAadharDocuments;
    private Integer acceptedAadharDocuments;
    private Integer rejectedAadharDocuments;


    //Pan Data
    private Integer numberOfPanDocuments;
    private Integer acceptedPanDocuments;
    private Integer rejectedPanDocuments;
}