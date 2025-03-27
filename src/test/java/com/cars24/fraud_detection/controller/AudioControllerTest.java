package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioControllerTest {

    @Mock
    private AudioService audioService;

    @InjectMocks
    private AudioController audioController;

    private AudioRequest audioRequest;
    private AudioResponse audioResponse;

    @BeforeEach
    void setUp() {
        audioRequest = new AudioRequest();
        audioResponse = new AudioResponse();
    }

    @Test
    void testUploadAudio_Success() throws IOException, AudioProcessingException {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[]{});
        when(audioService.processAudioRequest(any(AudioRequest.class))).thenReturn(audioResponse);

        ResponseEntity<AudioResponse> response = audioController.uploadAudio(file, "agent1", "lead1", "documentType");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(audioService).processAudioRequest(any(AudioRequest.class));
    }

    @Test
    void testUploadAudio_AudioProcessingException() throws IOException, AudioProcessingException {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[]{});
        when(audioService.processAudioRequest(any(AudioRequest.class))).thenThrow(new AudioProcessingException("Error"));

        ResponseEntity<AudioResponse> response = audioController.uploadAudio(file, "agent1", "lead1", "documentType");

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testUploadAudio_IOException() throws IOException, AudioProcessingException {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[]{});
        doThrow(new IOException("File error")).when(audioService).processAudioRequest(any(AudioRequest.class));

        ResponseEntity<AudioResponse> response = audioController.uploadAudio(file, "agent1", "lead1", "documentType");

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGetAudio_Success() throws AudioProcessingException {
        when(audioService.getAudioResults("123")).thenReturn(audioResponse);

        ResponseEntity<AudioResponse> response = audioController.getAudio("123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(audioService).getAudioResults("123");
    }

    @Test
    void testGetAudioFile_Success() {
        FileSystemResource mockFile = mock(FileSystemResource.class);
        when(audioService.getAudioFile("123")).thenReturn(ResponseEntity.ok(mockFile));

        ResponseEntity<FileSystemResource> response = audioController.getAudioFile("123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(audioService).getAudioFile("123");
    }

    @Test
    void testGetRecentAudios_Success() {
        when(audioService.getRecentAudios("lead1", 5)).thenReturn(Collections.singletonList("audio123"));

        ResponseEntity<List<String>> response = audioController.getRecentAudios("lead1", 5);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        assertEquals("audio123", response.getBody().get(0));
    }

    @Test
    void testGetRecentAudio_Success() {
        when(audioService.getRecentAudios("lead1", 1)).thenReturn(Collections.singletonList("latestAudio"));

        ResponseEntity<String> response = audioController.getRecentAudio("lead1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("latestAudio", response.getBody());
    }

    @Test
    void testGetRecentAudio_NotFound() {
        when(audioService.getRecentAudios("lead1", 1)).thenReturn(Collections.emptyList());

        ResponseEntity<String> response = audioController.getRecentAudio("lead1");

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }
}
