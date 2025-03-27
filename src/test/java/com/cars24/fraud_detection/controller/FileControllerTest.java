package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import com.cars24.fraud_detection.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private FileEntity fileEntity;
    private FileResponse fileResponse;

    @BeforeEach
    void setUp() {
        fileEntity = new FileEntity("file123", "agent1", "lead1", "file.pdf", "pdf");

        fileResponse = new FileResponse(
                "file123", "agent1", "lead1", "file.pdf", "pdf", "/path/to/file",
                "ACTIVE", true, null
        );
    }

    @Test
    void testUploadFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{});
        when(fileService.uploadFile(any(), any(), any(), any(), any())).thenReturn(fileEntity);

        ResponseEntity<FileResponse> response = fileController.uploadFile(file, "agent1", "lead1", "pdf");

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(fileService).uploadFile(any(), any(), any(), any(), any());
    }

    @Test
    void testGetFile_Success() {
        when(fileService.getFile("file123")).thenReturn(fileEntity);

        ResponseEntity<FileResponse> response = fileController.getFile("file123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("file123", response.getBody().getFileId());
    }

    @Test
    void testGetFile_NotFound() {
        when(fileService.getFile("file123")).thenReturn(null);

        ResponseEntity<FileResponse> response = fileController.getFile("file123");

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetFilesByAgentAndLead() {
        when(fileService.getFilesByAgentAndLead("agent1", "lead1")).thenReturn(Collections.singletonList(fileEntity));

        List<FileEntity> response = fileController.getFilesByAgentAndLead("agent1", "lead1");

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }

    @Test
    void testGetActiveFilesByAgentAndLead() {
        when(fileService.getActiveFilesByAgentAndLead("agent1", "lead1")).thenReturn(Collections.singletonList(fileEntity));

        List<FileEntity> response = fileController.getActiveFilesByAgentAndLead("agent1", "lead1");

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }
}
