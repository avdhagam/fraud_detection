package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.AgentDao;
import com.cars24.fraud_detection.data.entity.AgentEntity;
import com.cars24.fraud_detection.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AgentDaoImpl implements AgentDao {

    private final AgentRepository agentRepository;

    @Override
    public AgentEntity saveAgent(AgentEntity agent) {
        return agentRepository.save(agent);
    }

    @Override
    public Optional<AgentEntity> findAgentById(String agentId) {
        return agentRepository.findById(agentId);
    }

    @Override
    public Optional<AgentEntity> findByEmail(String email) {
        return agentRepository.findByEmail(email);
    }
}