// AudioRepository.java
        package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AudioRepository extends MongoRepository<AudioEntity, String> {

        // Find all audios by userReportId
        List<AudioEntity> findByUserReportId(String userReportId);

        @Query("{ 'userReportId': ?0 }")
        List<AudioEntity> findRecentAudiosByUserReportId(String userReportId, Pageable pageable);


}