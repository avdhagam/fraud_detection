package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioDaoImplTest {

    @Mock
    private AudioRepository audioRepository;

    @InjectMocks
    private AudioDaoImpl audioDao;

    private AudioEntity audioEntity;

    @BeforeEach
    void setUp() {
        audioEntity = new AudioEntity();
        audioEntity.setId("audio123");
        audioEntity.setLeadId("lead123");
    }

    @Test
    void testSaveAudio() {
        when(audioRepository.save(any(AudioEntity.class))).thenReturn(audioEntity);

        AudioEntity savedAudio = audioDao.saveAudio(audioEntity);

        assertNotNull(savedAudio);
        assertEquals("audio123", savedAudio.getId());
        verify(audioRepository).save(audioEntity);
    }

    @Test
    void testGetAudioById() {
        when(audioRepository.findById("audio123")).thenReturn(Optional.of(audioEntity));

        Optional<AudioEntity> foundAudio = audioDao.getAudioById("audio123");

        assertTrue(foundAudio.isPresent());
        assertEquals("audio123", foundAudio.get().getId());
    }

    @Test
    void testFindByLeadId() {
        when(audioRepository.findByLeadId("lead123")).thenReturn(List.of(audioEntity));

        List<AudioEntity> audioList = audioDao.findByLeadId("lead123");

        assertFalse(audioList.isEmpty());
        assertEquals(1, audioList.size());
    }

    @Test
    void testGetRecentAudiosByLeadId() {
        when(audioRepository.findByLeadIdOrderByTimestampDesc("lead123", PageRequest.of(0, 5)))
                .thenReturn(List.of(audioEntity));

        List<AudioEntity> recentAudios = audioDao.getRecentAudiosByLeadId("lead123", 5);

        assertFalse(recentAudios.isEmpty());
        assertEquals(1, recentAudios.size());
    }
}
