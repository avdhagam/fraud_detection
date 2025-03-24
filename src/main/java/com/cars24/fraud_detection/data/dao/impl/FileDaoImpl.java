package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import com.cars24.fraud_detection.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

}
