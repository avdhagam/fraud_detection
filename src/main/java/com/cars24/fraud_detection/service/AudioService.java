package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface
AudioService {
    ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String id);
    AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException;
//    AudioResponse getAudioResults(String id);
    List<AudioEntity> getAudiosByUserId(String userId);

    AudioResponse getAudioResult(String userId);
}