// AudioRepository.java
        package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AudioRepository extends MongoRepository<AudioEntity, String> {

        // Find all audios by userReportId


    List<AudioEntity> findByUserId(String userId);

}