package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AudioService {

    AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException;

    AudioResponse getAudioResults(String audioId) throws AudioProcessingException;

    List<AudioEntity> getAudiosByLeadId(String leadId);

    List<String> getRecentAudios(String leadId, int limit);

    String saveAudio(MultipartFile file, String uuid) throws AudioProcessingException;

    ResponseEntity<FileSystemResource> getAudioFile(String audioId);

    InsightsEntity getAudioInsights(String audioId) throws AudioProcessingException;
}