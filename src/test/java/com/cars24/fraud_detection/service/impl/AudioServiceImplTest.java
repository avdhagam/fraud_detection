package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioServiceImplTest {

    @Mock
    private AudioDao audioDao;

    @Mock
    private LeadDao leadDao;

    @Mock
    private WorkflowInitiator workflowInitiator;

    @Mock
    private DocumentTypeConfig documentTypeConfig;

    @InjectMocks
    private AudioServiceImpl audioService;

    private AudioRequest audioRequest;
    private LeadEntity leadEntity;
    private AudioEntity audioEntity;
    private AudioResponse audioResponse;
    private String uuid;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID().toString();
        audioRequest = new AudioRequest();
        audioRequest.setUuid(uuid);
        audioRequest.setLeadId("lead123");
        audioRequest.setAgentId("agent123");
        audioRequest.setDocumentType("REFERENCE_CALL");
        audioRequest.setAudioFile(new MockMultipartFile("audio", "audio.mp3", "audio/mpeg", new byte[]{1, 2, 3}));

        leadEntity = new LeadEntity();
        leadEntity.setId("lead123");

        audioEntity = new AudioEntity();
        audioEntity.setId(uuid);
        audioEntity.setLeadId("lead123");
        audioEntity.setAgentId("agent123");

        audioResponse = new AudioResponse();
        audioResponse.setId(uuid);
    }

    @Test
    void testProcessAudioRequest_Success() throws JsonProcessingException, AudioProcessingException {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.of(leadEntity));
        when(workflowInitiator.processAudio(any(AudioRequest.class))).thenReturn(audioResponse);
        when(audioDao.saveAudio(any(AudioEntity.class))).thenReturn(audioEntity);

        AudioResponse response = audioService.processAudioRequest(audioRequest);

        assertNotNull(response);
        assertEquals(uuid, response.getId());
        verify(audioDao, times(1)).saveAudio(any(AudioEntity.class));
    }

    @Test
    void testProcessAudioRequest_Fail_LeadNotFound() {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.empty());

        assertThrows(AudioProcessingException.class, () -> audioService.processAudioRequest(audioRequest));
    }

    @Test
    void testGetAudioResults_Success() throws AudioProcessingException {
        when(audioDao.getAudioById(uuid)).thenReturn(Optional.of(audioEntity));

        AudioResponse response = audioService.getAudioResults(uuid);

        assertNotNull(response);
        assertEquals(uuid, response.getId());
    }

    @Test
    void testGetAudioResults_Fail_NotFound() {
        when(audioDao.getAudioById(uuid)).thenReturn(Optional.empty());

        assertThrows(AudioProcessingException.class, () -> audioService.getAudioResults(uuid));
    }
}
