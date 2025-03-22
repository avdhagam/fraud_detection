package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AgentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AgentRepository extends MongoRepository<AgentEntity, String> {
    Optional<AgentEntity> findByEmail(String email);
}