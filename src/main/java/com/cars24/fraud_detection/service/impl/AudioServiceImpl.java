package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AudioDao audioDao;
    @Autowired
    private WorkflowInitiator workflowInitiator;
    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());
    private static final String STORAGE_PATH = "src/main/resources/audio_storage";

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException {
        logger.info("Received AudioRequest object: " + audioRequest);
        MultipartFile file = audioRequest.getAudioFile();

        // Save the audio file using the new logic
        String filePath = saveAudio(file);

        // Correctly extract the UUID using Paths
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String uuid = fileName.replace(".mp3", "");

        audioRequest.setFilepath(filePath);
        audioRequest.setUuid(uuid);

        AudioResponse audioResponse = workflowInitiator.processAudio(audioRequest);
        logger.info("Received AudioResponse object: " + audioResponse);

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
        logger.info("Created AudioEntity object: " + audioEntity);

        audioDao.saveAudio(audioEntity);
        logger.info("Saved AudioEntity object to database");

        return audioResponse;
    }

    private String saveAudio(MultipartFile file) throws AudioProcessingException {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                logger.severe("Failed to save audio file: File is empty");
                throw new AudioProcessingException("Failed to store audio file: File is empty");
            }

            // Ensure the storage directory exists
            Path storagePath = Paths.get(STORAGE_PATH);
            Files.createDirectories(storagePath);

            // Generate a unique file name
            String uniqueFileName = UUID.randomUUID().toString() + ".mp3";

            // Properly construct path using Path API
            Path destinationFile = storagePath.resolve(uniqueFileName);

            logger.info("Attempting to save file to: " + destinationFile.toString());

            // Save the file to disk
            Files.copy(file.getInputStream(), destinationFile);

            logger.info("Audio file saved successfully to " + destinationFile.toString());

            return destinationFile.toString();
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
        }
    }
}