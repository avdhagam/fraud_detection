package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AudioServiceImpl implements AudioService {

    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());
    private static final String STORAGE_PATH = "src/main/resources/audio_storage";

    private final AudioDao audioDao;
    private final WorkflowInitiator workflowInitiator;

    public AudioServiceImpl(AudioDao audioDao, WorkflowInitiator workflowInitiator) {
        this.audioDao = audioDao;
        this.workflowInitiator = workflowInitiator;
    }

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException {
        logger.info(() -> "Processing AudioRequest: " + audioRequest);

        MultipartFile file = audioRequest.getAudioFile();
        String filePath = saveAudio(file);

        String uuid = extractUuidFromPath(filePath);
        audioRequest.setFilepath(filePath);
        audioRequest.setUuid(uuid);

        AudioResponse audioResponse = workflowInitiator.processAudio(audioRequest);
<<<<<<< HEAD
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
        audioEntity.setStatus(audioResponse.getStatus());
        logger.info("Created AudioEntity object: " + audioEntity);
=======
        logger.info(() -> "Received AudioResponse: " + audioResponse);
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4

        AudioEntity audioEntity = mapToEntity(audioResponse);
        audioDao.saveAudio(audioEntity);

        logger.info(() -> "Saved AudioEntity: " + audioEntity);
        return audioResponse;
    }

    @Override
    public AudioResponse getAudioResults(String id) {
        logger.info(() -> "Fetching audio analysis for ID: " + id);

        AudioEntity audioEntity = audioDao.getAudioById(id)
                .orElseThrow(() -> {
                    logger.warning(() -> "Audio analysis not found for ID: " + id);
                    return new RuntimeException("Audio analysis not found for ID: " + id);
                });

        logger.info(() -> "Fetched AudioEntity: " + audioEntity);
        return mapToResponse(audioEntity);
    }

    @Override
    public List<AudioEntity> getAudiosByUserId(String userId) {
        return audioDao.getAudiosByUserId(userId);
    }

    private String saveAudio(MultipartFile file) throws AudioProcessingException {
        if (file.isEmpty()) {
            logger.severe("Failed to save audio file: File is empty");
            throw new AudioProcessingException("Failed to store audio file: File is empty");
        }

        try {
            Path storagePath = Paths.get(STORAGE_PATH);
            Files.createDirectories(storagePath);

            String uniqueFileName = UUID.randomUUID() + ".mp3";
            Path destinationFile = storagePath.resolve(uniqueFileName);

            logger.info(() -> "Saving file to: " + destinationFile);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info(() -> "Audio file saved successfully at " + destinationFile);

            return destinationFile.toString();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving audio file", e);
            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
        }
    }

    private String extractUuidFromPath(String filePath) {
        return Paths.get(filePath).getFileName().toString().replace(".mp3", "");
    }

    private AudioEntity mapToEntity(AudioResponse audioResponse) {
        AudioEntity entity = new AudioEntity();
        entity.setId(audioResponse.getUuid());
        entity.setUserReportId(audioResponse.getUserReportId());
        entity.setLlmExtraction(audioResponse.getLlmExtraction());
        entity.setTranscript(audioResponse.getTranscript());
        entity.setReferenceName(audioResponse.getReferenceName());
        entity.setSubjectName(audioResponse.getSubjectName());
        entity.setSubjectAddress(audioResponse.getSubjectAddress());
        entity.setRelationToSubject(audioResponse.getRelationToSubject());
        entity.setSubjectOccupation(audioResponse.getSubjectOccupation());
        entity.setOverallScore(audioResponse.getOverallScore());
        entity.setExplanation(audioResponse.getExplanation());
        entity.setFieldByFieldScores(audioResponse.getFieldByFieldScores());
        entity.setAudioAnalysis(audioResponse.getAudioAnalysis());
        entity.setStatus(audioResponse.getStatus());
        return entity;
    }

    private AudioResponse mapToResponse(AudioEntity entity) {
        AudioResponse response = new AudioResponse();
        response.setUuid(entity.getId());
        response.setUserReportId(entity.getUserReportId());
        response.setLlmExtraction(entity.getLlmExtraction());
        response.setTranscript(entity.getTranscript());
        response.setReferenceName(entity.getReferenceName());
        response.setSubjectName(entity.getSubjectName());
        response.setSubjectAddress(entity.getSubjectAddress());
        response.setRelationToSubject(entity.getRelationToSubject());
        response.setSubjectOccupation(entity.getSubjectOccupation());
        response.setOverallScore(entity.getOverallScore());
        response.setExplanation(entity.getExplanation());
        response.setFieldByFieldScores(entity.getFieldByFieldScores());
        response.setAudioAnalysis(entity.getAudioAnalysis());
        response.setStatus(entity.getStatus());
        return response;
    }
}
