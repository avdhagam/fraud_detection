package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.AgentEntity;
import com.cars24.fraud_detection.data.request.AgentRequest;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.response.AgentAnalysis;
import com.cars24.fraud_detection.data.response.AgentResponse;
import com.cars24.fraud_detection.data.response.LoginResponse;

public interface AgentService {
    AgentResponse registerAgent(AgentRequest agentRequest);
    LoginResponse loginAgent(LoginRequest loginRequest);
    AgentEntity getAgentById(String agentId); //returns AgentEntity

    AgentAnalysis analyzeAgent(String agentId);
}