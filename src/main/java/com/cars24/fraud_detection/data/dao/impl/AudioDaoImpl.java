package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AudioDaoImpl implements AudioDao {

    private final AudioRepository audioRepo;

    @Override
    public void saveAudio(AudioEntity audio) {
        audioRepo.save(audio);
    }

    @Override
    public Optional<AudioEntity> getAudioById(String audioId) {
        return audioRepo.findById(audioId);
    }
}