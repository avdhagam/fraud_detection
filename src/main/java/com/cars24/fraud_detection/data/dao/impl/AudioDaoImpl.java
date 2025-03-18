package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AudioDaoImpl implements AudioDao {

    private final AudioRepository audioRepo;

    @Override
    public void saveAudio(AudioEntity audio) {
        if (audio.getUserReportId() == null) {
            throw new IllegalArgumentException("User ID cannot be null when saving audio!");
        }
        audioRepo.save(audio);
    }

    @Override
    public Optional<AudioEntity> getAudioById(String audioId) {
        log.info("Fetching AudioEntity with ID: " + audioId);
        return audioRepo.findById(audioId);
    }

    @Override
    public List<AudioEntity> getAudiosByUserId(String userReportId) {
        return audioRepo.findByUserReportId(userReportId);
    }

}


