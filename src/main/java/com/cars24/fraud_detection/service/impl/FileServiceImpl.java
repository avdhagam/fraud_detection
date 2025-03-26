package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.data.response.FileResponse; // Import FileResponse
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.service.FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String AUDIO_STORAGE_PATH = "src/main/resources/audio_storage"; // Correct Path
    private static final String DOCUMENT_STORAGE_PATH = "src/main/resources/document_storage";  // Correct Path
    private final FileDao fileDao;
    private final DocumentTypeConfig documentTypeConfig;
    private final AsyncFileProcessingService asyncFileProcessingService;

    private final DocumentService documentService;
    private final AudioService audioService;

    @Override
    @Transactional
    public FileEntity uploadFile(String agentId, String leadId, String fileType, String originalFilename, byte[] fileData) {
        // **Determine storage path based on file type**
        String storagePath = determineStoragePath(fileType);

        // Ensure directory exists
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // **Extract file extension from original filename**
        String fileExtension = extractFileExtension(originalFilename);
        if (fileExtension == null) {
            throw new IllegalArgumentException("Invalid file type");
        }

        // **Generate unique filename**
        String filename = UUID.randomUUID().toString() + "-" + fileType + fileExtension;
        String filePath = Paths.get(storagePath, filename).toString();  // Correct filePath

        // **Save file to local storage**
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);
        } catch (IOException e) {
            log.error("Error saving file: {}", filePath, e);
            throw new RuntimeException("File upload failed");
        }

        // **Mark all existing files as INACTIVE**
        List<FileEntity> existingFiles = fileDao.findByAgentIdAndLeadIdAndFileType(agentId, leadId, fileType);
        for (FileEntity existingFile : existingFiles) {
            existingFile.setIsActive(Boolean.FALSE); // Mark previous files as inactive
            fileDao.save(existingFile);
        }

        //  **Create and Save New FileEntity**
        FileEntity newFile = new FileEntity(agentId, leadId, filename, fileType, originalFilename); // Add originalFilename
        newFile.setStatus("PENDING");  // Default status
        newFile.setIsActive(Boolean.TRUE);  // Mark new file as active
        newFile.setUploadedAt(LocalDateTime.now());
        newFile.setFilePath(filePath);  // Save full file path

        FileEntity savedFile = fileDao.save(newFile);


        if (documentTypeConfig.getDocumentDisplayName("REFERENCE_CALL").equalsIgnoreCase(fileType)) {
            asyncFileProcessingService.processAsyncAudio(savedFile);
        } else {
            asyncFileProcessingService.processAsyncDocument(savedFile);
        }

        log.info("File uploaded successfully and async processing started.");


        return savedFile;
    }



    @Transactional
    @Override
    public List<FileResponse> uploadMultipleFiles(String agentId, String leadId, List<String> fileTypes, List<MultipartFile> files) {
        List<FileResponse> responses = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileType = fileTypes.get(i);  // Get corresponding file type

            try {
                byte[] fileData = file.getBytes();
                FileEntity savedFile = uploadFile(agentId, leadId, fileType, file.getOriginalFilename(), fileData);

                responses.add(new FileResponse(
                        savedFile.getFileId(),
                        savedFile.getAgentId(),
                        savedFile.getLeadId(),
                        savedFile.getOriginalFilename(),
                        savedFile.getFileType(),
                        savedFile.getFilePath(),
                        savedFile.getStatus(),
                        savedFile.getIsActive(),
                        savedFile.getUploadedAt()
                ));
            } catch (IOException e) {
                log.error("Error reading file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to read file: " + file.getOriginalFilename());
            }
        }

        return responses;
    }


    private String determineStoragePath(String fileType) {
        String referenceCallType = documentTypeConfig.getDocumentDisplayName("REFERENCE_CALL");
        if (referenceCallType.equalsIgnoreCase(fileType)) {
            return AUDIO_STORAGE_PATH;
        } else {
            return DOCUMENT_STORAGE_PATH;
        }
    }



    private String extractFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }

    private byte[] readFileAsBytes(String filePath) throws IOException {
        File file = new File(filePath);
        return java.nio.file.Files.readAllBytes(file.toPath());
    }

    @Override
    public FileEntity getFile(String fileId) {
        return fileDao.findById(fileId).orElse(null);
    }

    @Override
    public List<FileEntity> getFilesByAgentAndLead(String agentId, String leadId, String fileType) {
        return fileDao.findByAgentIdAndLeadIdAndFileType(agentId, leadId, fileType);
    }


    @Override
    public List<FileEntity> getFilesByAgentAndLead(String agentId, String leadId) {
        return fileDao.findByAgentIdAndLeadId(agentId, leadId);
    }

    @Override
    public List<FileEntity> getActiveFilesByAgentAndLead(String agentId, String leadId) {
        return fileDao.findByAgentIdAndLeadIdAndIsActiveTrue(agentId, leadId);
    }


}






