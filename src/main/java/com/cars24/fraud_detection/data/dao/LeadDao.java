package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import java.util.List;
import java.util.Optional;

public interface LeadDao {
    LeadEntity saveLead(LeadEntity lead);
    Optional<LeadEntity> findLeadById(String leadId);
    List<LeadEntity> findByAgentId(String agentId);
    List<AudioEntity> findAudioByAgentAndLead(String agentId,String leadId);
    List<DocumentEntity> findDocumentsByAgentAndLead(String agentId,String leadId);
    LeadEntity findByAgentIdAndId(String agentId, String leadId);
}