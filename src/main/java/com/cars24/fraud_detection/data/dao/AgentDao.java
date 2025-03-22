package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.AgentEntity;
import java.util.Optional;

public interface AgentDao {
    AgentEntity saveAgent(AgentEntity agent);
    Optional<AgentEntity> findAgentById(String agentId);
    Optional<AgentEntity> findByEmail(String email);
}