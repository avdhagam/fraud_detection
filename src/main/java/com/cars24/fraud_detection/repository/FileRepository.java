package com.cars24.fraud_detection.repository;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface FileRepository extends MongoRepository<FileEntity, String> {
    List<FileEntity> findByAgentIdAndLeadIdAndFileType(String agentId, String leadId, String fileType);
}
