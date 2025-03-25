package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AudioRepository extends MongoRepository<AudioEntity, String> {
    List<AudioEntity> findByLeadId(String leadId);
    List<AudioEntity> findByLeadIdOrderByTimestampDesc(String leadId, Pageable pageable);
    List<AudioEntity> findByAgentIdAndLeadId(String agentId,String leadId);
}