package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;

import java.util.List;
import java.util.Optional;

public interface FileDao {
    FileEntity save(FileEntity fileEntity);

    Optional<FileEntity> findById(String id);

    void updateStatus(String fileId, String status);

    List<FileEntity> findByAgentIdAndLeadIdAndFileType(String agentId, String leadId, String fileType);

    List<FileEntity> findByAgentIdAndLeadIdAndIsActive(String agentId, String leadId, Boolean isActive);

    List<FileEntity> findByAgentIdAndLeadId(String agentId, String leadId);

    List<FileEntity> findByAgentIdAndLeadIdAndIsActiveTrue(String agentId, String leadId);
}
