package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AudioDao audioDao;
    @Autowired
    private WorkflowInitiator workflowInitiator;
    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException {
        logger.info("Received AudioRequest object: " + audioRequest);
        MultipartFile file = audioRequest.getAudioFile();
        String uuid = UUID.randomUUID().toString();
        String filePath = "/Users/avanidhagam/Desktop/work/fraud_detection/src/main/resources/audio_storage/" + uuid + ".mp3";
        File audioFile = new File(filePath);
        try {
            file.transferTo(audioFile);
            logger.info("Audio file saved successfully to " + filePath);
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
        }

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
}