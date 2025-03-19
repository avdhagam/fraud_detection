package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AudioService {
    AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException;
    AudioResponse getAudioResults(String id);
    List<AudioEntity> getAudiosByUserId(String userId);
    //AudioResponse processUserAudio(AudioRequest audioRequest, String userReportId) throws JsonProcessingException, AudioProcessingException;
    List<String> getRecentAudios(String userId, int limit);
}