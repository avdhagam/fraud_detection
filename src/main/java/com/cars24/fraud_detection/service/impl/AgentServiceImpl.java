package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AgentDao;
import com.cars24.fraud_detection.data.entity.AgentEntity;
import com.cars24.fraud_detection.data.request.AgentRequest;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.response.AgentResponse;
import com.cars24.fraud_detection.data.response.LoginResponse;
import com.cars24.fraud_detection.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentDao agentDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AgentResponse registerAgent(AgentRequest agentRequest) {
        if (agentRequest.getName() == null || agentRequest.getName().isBlank()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty!");
        }

        AgentEntity agentEntity = new AgentEntity();
        agentEntity.setName(agentRequest.getName());
        agentEntity.setEmail(agentRequest.getEmail());
        agentEntity.setPassword(passwordEncoder.encode(agentRequest.getPassword())); // Hash the password

        AgentEntity savedAgent = agentDao.saveAgent(agentEntity);

        // Map the saved AgentEntity to AgentResponse
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setId(savedAgent.getId());
        agentResponse.setName(savedAgent.getName());
        agentResponse.setEmail(savedAgent.getEmail());

        return agentResponse;
    }

    @Override
    public LoginResponse loginAgent(LoginRequest loginRequest) {
        AgentEntity agentEntity = agentDao.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), agentEntity.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(agentEntity.getId());
        loginResponse.setName(agentEntity.getName());
        loginResponse.setEmail(agentEntity.getEmail());

        return loginResponse;
    }

    @Override
    public AgentEntity getAgentById(String agentId) {
        return agentDao.findAgentById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with ID: " + agentId));
    }
}