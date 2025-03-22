package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LeadDaoImpl implements LeadDao {

    private final LeadRepository leadRepository;

    @Override
    public LeadEntity saveLead(LeadEntity lead) {
        return leadRepository.save(lead);
    }

    @Override
    public Optional<LeadEntity> findLeadById(String leadId) {
        return leadRepository.findById(leadId);
    }

    @Override
    public List<LeadEntity> findByAgentId(String agentId) {
        return leadRepository.findByAgentId(agentId);
    }
}