package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import java.util.List;
import java.util.Optional;

public interface AudioDao {
    AudioEntity saveAudio(AudioEntity audio);
    Optional<AudioEntity> getAudioById(String audioId);
    List<AudioEntity> findByLeadId(String leadId);
    List<AudioEntity> getRecentAudiosByLeadId(String leadId, int limit);
}