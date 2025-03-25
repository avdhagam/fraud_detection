package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import com.cars24.fraud_detection.repository.DocumentRepository;
import com.cars24.fraud_detection.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LeadDaoImpl implements LeadDao {

    private final LeadRepository leadRepository;
    private final AudioRepository audioRepository;
    private final DocumentRepository documentRepository;


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

    @Override
    public List<AudioEntity> findAudioByAgentAndLead(String agentId,String leadId) {
        return audioRepository.findByAgentIdAndLeadId(agentId,leadId);
    }

    @Override
    public List<DocumentEntity> findDocumentsByAgentAndLead(String agentId,String leadId) {
        return documentRepository.findByAgentIdAndLeadId(agentId,leadId);
    }

    @Override
    public LeadEntity findByAgentIdAndId(String agentId,String leadId) {
        return leadRepository.findByAgentIdAndId(agentId,leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found for given leadId and agentId"));
    }
}