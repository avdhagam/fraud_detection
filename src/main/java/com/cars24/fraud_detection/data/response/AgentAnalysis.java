package com.cars24.fraud_detection.data.response;

import lombok.Data;

@Data
public class AgentAnalysis {

    //Reference Call Metrics
    private double averageAgentTalkTime;
    private double averageReferenceTalkTime;
    private int totalReferenceCalls;
    private int acceptedReferenceCalls;
    private int rejectedReferenceCalls;

    //Aadhar Metrics
    private int totalAadhars;
    private int acceptedAadhars;
    private int rejectedAadhars;

    //Pan Metrics
    private int totalPans;
    private int acceptedPans;
    private int rejectedPans;
}