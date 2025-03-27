package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncFileProcessingServiceTest {

    @Mock
    private FileDao fileDao;

    @Mock
    private AudioService audioService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private AsyncFileProcessingService asyncFileProcessingService;

    private FileEntity fileEntity;
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        fileEntity = new FileEntity(
                "agent1",
                "lead1",
                "audio.mp3",
                "audio/mp3",
                "path/to/audio.mp3"
        );

        mockMultipartFile = new MockMultipartFile(
                "file",
                "audio.mp3",
                "audio/mp3",
                new byte[]{1, 2, 3, 4}
        );
    }

    @Test
    void testProcessAsyncAudio_Success() throws Exception {
        AudioResponse audioResponse = new AudioResponse();
        when(audioService.processAudioRequest(any(AudioRequest.class))).thenReturn(audioResponse);

        asyncFileProcessingService.processAsyncAudio(fileEntity);

        verify(fileDao, times(1)).updateStatus(fileEntity.getFileId(), "PROCESSED");
    }

    @Test
    void testProcessAsyncAudio_Failure() throws Exception {
        when(audioService.processAudioRequest(any(AudioRequest.class))).thenThrow(new IOException());

        asyncFileProcessingService.processAsyncAudio(fileEntity);

        verify(fileDao, times(1)).updateStatus(fileEntity.getFileId(), "FAILED");
    }

    @Test
    void testProcessAsyncDocument_Success() {
        DocumentResponse documentResponse = new DocumentResponse();
        when(documentService.processDocument(any(DocumentRequest.class))).thenReturn(documentResponse);

        asyncFileProcessingService.processAsyncDocument(fileEntity);

        verify(fileDao, times(1)).updateStatus(fileEntity.getFileId(), "PROCESSED");
    }

    @Test
    void testProcessAsyncDocument_Failure() {
        when(documentService.processDocument(any(DocumentRequest.class))).thenThrow(new RuntimeException());

        asyncFileProcessingService.processAsyncDocument(fileEntity);

        verify(fileDao, times(1)).updateStatus(fileEntity.getFileId(), "FAILED");
    }
}
