package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import com.cars24.fraud_detection.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileDaoImpl implements FileDao {

    private final FileRepository fileRepository;

    @Override
    public FileEntity save(FileEntity fileEntity) {
        return fileRepository.save(fileEntity);
    }

    @Override
    public Optional<FileEntity> findById(String id) {
        return fileRepository.findById(id);
    }

    @Override
    public void updateStatus(String fileId, String status) {
        fileRepository.findById(fileId).ifPresent(file -> {
            file.setStatus(status);
            fileRepository.save(file);
        });
    }

    @Override
    public List<FileEntity> findByAgentIdAndLeadIdAndFileType(String agentId, String leadId, String fileType) {
       return fileRepository.findByAgentIdAndLeadIdAndFileType(agentId, leadId, fileType);

    }

    @Override
    public List<FileEntity> findByAgentIdAndLeadIdAndIsActive(String agentId, String leadId, Boolean isActive) {
        return fileRepository.findByAgentIdAndLeadIdAndIsActive(agentId, leadId, isActive);
    }

    @Override
    public List<FileEntity> findByAgentIdAndLeadId(String agentId, String leadId) {
        return fileRepository.findByAgentIdAndLeadId(agentId, leadId);
    }

    @Override
    public List<FileEntity> findByAgentIdAndLeadIdAndIsActiveTrue(String agentId, String leadId) {
        return fileRepository.findByAgentIdAndLeadIdAndIsActiveTrue(agentId, leadId);
    }



}
