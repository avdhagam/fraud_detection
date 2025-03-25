package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    FileEntity uploadFile(String agentId, String leadId, String fileType, String originalFilename, byte[] fileData);

    void processAsyncAudio(FileEntity fileEntity);

    void processAsyncDocument(FileEntity fileEntity);

    FileEntity getFile(String fileId);

    List<FileEntity> getFilesByAgentAndLead(String agentId, String leadId, String fileType);

    List<FileResponse> uploadMultipleFiles(String agentId, String leadId, List<String> fileTypes, List<MultipartFile> files);
}
