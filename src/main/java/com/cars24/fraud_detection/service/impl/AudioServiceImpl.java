package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.utils.AudioServiceUtils;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AudioDao audioDao;
    @Autowired
    @Lazy
    private WorkflowInitiator workflowInitiator;
    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());
    private static final String STORAGE_PATH = "src/main/resources/audio_storage";

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException {
        logger.info("Received AudioRequest object: " + audioRequest);
        MultipartFile file = audioRequest.getAudioFile();

        // Save the audio file using the new logic
        String filePath = AudioServiceUtils.saveAudio(file);

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
        audioEntity.setStatus(audioResponse.getStatus());
        logger.info("Created AudioEntity object: " + audioEntity);

        audioDao.saveAudio(audioEntity);
        logger.info("Saved AudioEntity object to database");

        return audioResponse;
    }


    @Override
    public AudioResponse getAudioResults(String id) {
        logger.info("Fetching audio analysis for ID: " + id);

        Optional<AudioEntity> audioEntityOpt = audioDao.getAudioById(id);
        if (audioEntityOpt.isEmpty()) {
            logger.warning("Audio analysis not found for ID: " + id);
            throw new RuntimeException("Audio analysis not found for ID: " + id);
        }

        AudioEntity audioEntity = audioEntityOpt.get();
        logger.info("Fetched audio entity: " + audioEntity);

        return AudioServiceUtils.mapToResponse(audioEntity);
    }


}