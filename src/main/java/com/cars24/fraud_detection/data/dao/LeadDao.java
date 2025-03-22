package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import java.util.List;
import java.util.Optional;

public interface LeadDao {
    LeadEntity saveLead(LeadEntity lead);
    Optional<LeadEntity> findLeadById(String leadId);
    List<LeadEntity> findByAgentId(String agentId);
}