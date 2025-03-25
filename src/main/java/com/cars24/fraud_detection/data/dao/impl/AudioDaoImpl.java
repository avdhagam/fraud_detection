package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AudioDaoImpl implements AudioDao {

    private final AudioRepository audioRepository;

    @Override
    public AudioEntity saveAudio(AudioEntity audio) {
        return audioRepository.save(audio);
    }

    @Override
    public Optional<AudioEntity> getAudioById(String audioId) {
        return audioRepository.findById(audioId);
    }

    @Override
    public List<AudioEntity> findByLeadId(String leadId) {
        return audioRepository.findByLeadId(leadId);
    }

    @Override
    public List<AudioEntity> getRecentAudiosByLeadId(String leadId, int limit) {
        return audioRepository.findByLeadIdOrderByTimestampDesc(leadId, PageRequest.of(0, limit));
    }
}