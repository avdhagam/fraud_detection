// AudioRepository.java
        package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AudioRepository extends MongoRepository<AudioEntity, String> {
}