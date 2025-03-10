package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.AudioEntity;

import java.util.Optional;

public interface AudioDao {
    void saveAudio(AudioEntity audio);
    Optional<AudioEntity> getAudioById(String audioId);
}