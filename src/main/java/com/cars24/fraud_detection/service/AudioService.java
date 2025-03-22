package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.cars24.fraud_detection.exception.AudioProcessingException;

import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import java.io.IOException;


public interface AudioService {
    AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException;
    AudioResponse getAudioResults(String audioId);
    List<AudioEntity> getAudiosByLeadId(String leadId);
    List<String> getRecentAudios(String leadId, int limit);
    ResponseEntity<FileSystemResource> getAudioFile(String audioId);
    InsightsEntity getAudioInsights(String audioId); // Added this line
}