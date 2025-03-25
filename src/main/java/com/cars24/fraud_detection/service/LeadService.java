package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadNameEmail;
import com.cars24.fraud_detection.data.response.LeadResponse;

import java.util.List;

public interface LeadService {
    LeadResponse createLead(LeadRequest leadRequest);
    LeadEntity getLeadById(String leadId);
    List<LeadEntity> getLeadsByAgentId(String agentId);
    List<InsightsEntity> getLeadInsights(String leadId);
    List<LeadNameEmail> getLeadNameEmailByAgentId(String agentId);
    String getLeadName(String leadId);
    LeadResponse getLeadDetails(String agentId,String leadId);
    LeadResponse getActiveLeadDetails(String agentId, String leadId);
}