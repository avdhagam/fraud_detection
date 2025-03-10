package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AudioServiceImpl implements AudioService {

    private static final Logger logger = LoggerFactory.getLogger(AudioServiceImpl.class);

    private final AudioDao audioDao;
    private final WorkflowInitiator workflowInitiator;

    @Value("${audio.storage.path:/tmp/audio_storage}")
    private String audioStoragePath;

    @Autowired
    public AudioServiceImpl(AudioDao audioDao, WorkflowInitiator workflowInitiator) {
        this.audioDao = audioDao;
        this.workflowInitiator = workflowInitiator;
    }

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws AudioProcessingException {
        validateRequest(audioRequest);

        String uuid = UUID.randomUUID().toString();
        audioRequest.setUuid(uuid);

        String filePath = saveAudioFile(audioRequest.getAudioFile(), uuid);
        audioRequest.setFilepath(filePath);

        try {
            AudioResponse audioResponse = workflowInitiator.processAudio(audioRequest);
            logger.info("Audio processing complete for UUID: {}", uuid);

            AudioEntity audioEntity = mapToAudioEntity(audioResponse);
            audioDao.saveAudio(audioEntity);
            logger.info("Audio record saved to database with UUID: {}", uuid);

            return audioResponse;
        } catch (JsonProcessingException e) {
            logger.error("JSON processing error for audio UUID: {}", uuid, e);
            throw new AudioProcessingException("Error processing audio JSON data", e);
        } catch (Exception e) {
            logger.error("Unexpected error processing audio UUID: {}", uuid, e);
            throw new AudioProcessingException("Unexpected error during audio processing", e);
        }
    }

    private void validateRequest(AudioRequest audioRequest) throws AudioProcessingException {
        if (audioRequest == null) {
            throw new AudioProcessingException("Audio request cannot be null");
        }

        if (audioRequest.getAudioFile() == null || audioRequest.getAudioFile().isEmpty()) {
            throw new AudioProcessingException("Audio file is required");
        }
    }

    private String saveAudioFile(MultipartFile file, String uuid) throws AudioProcessingException {
        try {
            // Ensure directory exists
            Path directoryPath = Paths.get(audioStoragePath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);

            Path filePath = directoryPath.resolve(uuid + fileExtension);
            Files.copy(file.getInputStream(), filePath);

            logger.info("Audio file saved successfully to {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            logger.error("Failed to save audio file for UUID: {}", uuid, e);
            throw new AudioProcessingException("Error saving audio file", e);
        }
    }

    private String getFileExtension(String filename) {
        return filename.contains(".") ?
                filename.substring(filename.lastIndexOf(".")) :
                ".mp3"; // Default extension
    }

    private AudioEntity mapToAudioEntity(AudioResponse audioResponse) {
        AudioEntity audioEntity = new AudioEntity();
        audioEntity.setId(audioResponse.getUuid());
        audioEntity.setTranscript(audioResponse.getTranscript());
        audioEntity.setReferenceName(audioResponse.getReferenceName());
        audioEntity.setSubjectName(audioResponse.getSubjectName());
        audioEntity.setSubjectAddress(audioResponse.getSubjectAddress());
        audioEntity.setRelationToSubject(audioResponse.getRelationToSubject());
        audioEntity.setSubjectOccupation(audioResponse.getSubjectOccupation());
        audioEntity.setOverallScore(audioResponse.getOverallScore());
        audioEntity.setExplanation(audioResponse.getExplanation());
        audioEntity.setFieldByFieldScores(audioResponse.getFieldByFieldScores());
        audioEntity.setAudioAnalysis(audioResponse.getAudioAnalysis());
        return audioEntity;
    }
}