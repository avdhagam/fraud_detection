package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeadRepository extends MongoRepository<LeadEntity, String> {
    List<LeadEntity> findByAgentId(String agentId);
    Optional<LeadEntity> findByAgentIdAndId(String agentId,String leadId);
}