package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface FileRepository extends MongoRepository<FileEntity, String> {
    List<FileEntity> findByAgentIdAndLeadIdAndFileType(String agentId, String leadId, String fileType);

   // @Query("{ 'agentId': ?0, 'leadId': ?1, 'isActive': { $ne: false } }")
    List<FileEntity> findByAgentIdAndLeadIdAndIsActive(String agentId, String leadId, Boolean isActive);

}
