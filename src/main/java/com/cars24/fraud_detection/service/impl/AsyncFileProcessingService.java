package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncFileProcessingService {

    private final FileDao fileDao;
    private final AudioService audioService;
    private final DocumentService documentService;

    @Async
    public void processAsyncAudio(FileEntity fileEntity) {
        log.info("Processing audio file asynchronously: {}", fileEntity.getFileId());

        try {
            byte[] fileData = readFileAsBytes(fileEntity.getFilePath());

            MultipartFile multipartFile = new MockMultipartFile(
                    fileEntity.getOriginalFilename(),
                    fileEntity.getOriginalFilename(),
                    "audio/mp3",
                    fileData
            );

            AudioRequest audioRequest = new AudioRequest();
            audioRequest.setUuid(fileEntity.getFileId());
            audioRequest.setAgentId(fileEntity.getAgentId());
            audioRequest.setLeadId(fileEntity.getLeadId());
            audioRequest.setAudioFile(multipartFile);
            audioRequest.setDocumentType(fileEntity.getFileType());

            // Call the audio processing service
            AudioResponse response = audioService.processAudioRequest(audioRequest);
            log.info("Audio processing completed: {}", response);

            fileDao.updateStatus(fileEntity.getFileId(), "PROCESSED");
            log.info("Audio file processed successfully: {}", fileEntity.getFileId());

        } catch (IOException | AudioProcessingException e) {
            log.error("Error processing audio file: {}", fileEntity.getFileId(), e);
            fileDao.updateStatus(fileEntity.getFileId(), "FAILED");
        }
    }

    @Async
    public void processAsyncDocument(FileEntity fileEntity) {
        log.info("Processing document file asynchronously: {}", fileEntity.getFileId());

        try {
            byte[] fileData = readFileAsBytes(fileEntity.getFilePath());

            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setAgentId(fileEntity.getAgentId());
            documentRequest.setLeadId(fileEntity.getLeadId());
            documentRequest.setDocumentType(fileEntity.getFileType());
            documentRequest.setFileName(fileEntity.getOriginalFilename());
            documentRequest.setDocumentData(fileData);

            // Call the document processing service
            DocumentResponse response = documentService.processDocument(documentRequest);
            log.info("Document processing completed: {}", response);

            fileDao.updateStatus(fileEntity.getFileId(), "PROCESSED");
            log.info("Document file processed successfully: {}", fileEntity.getFileId());

        } catch (Exception e) {
            log.error("Error processing document file: {}", fileEntity.getFileId(), e);
            fileDao.updateStatus(fileEntity.getFileId(), "FAILED");
        }
    }

    private byte[] readFileAsBytes(String filePath) throws IOException {
        return Files.readAllBytes(new File(filePath).toPath());
    }
}