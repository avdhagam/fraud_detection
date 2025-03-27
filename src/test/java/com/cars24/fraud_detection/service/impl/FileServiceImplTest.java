package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import com.cars24.fraud_detection.service.impl.AsyncFileProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileDao fileDao;

    @Mock
    private DocumentTypeConfig documentTypeConfig;

    @Mock
    private AsyncFileProcessingService asyncFileProcessingService;

    @InjectMocks
    private FileServiceImpl fileService;

    private FileEntity fileEntity;
    private String fileId;
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        fileId = UUID.randomUUID().toString();

        fileEntity = new FileEntity(
                "agent1",
                "lead1",
                "document.pdf",
                "PDF",
                "path/to/document.pdf"
        );

        mockMultipartFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4}
        );
    }

    @Test
    void testUploadFile_Success() throws IOException {
        when(fileDao.findByAgentIdAndLeadIdAndFileType(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(fileDao.save(any(FileEntity.class))).thenReturn(fileEntity);

        FileEntity savedFile = fileService.uploadFile(
                "agent1",
                "lead1",
                "PDF",
                "document.pdf",
                mockMultipartFile.getBytes()
        );

        assertNotNull(savedFile);
        assertEquals("PDF", savedFile.getFileType());
        verify(fileDao, times(1)).save(any(FileEntity.class));
    }

    @Test
    void testGetFile_Success() {
        when(fileDao.findById(fileId)).thenReturn(Optional.of(fileEntity));

        FileEntity foundFile = fileService.getFile(fileId);

        assertNotNull(foundFile);
        assertEquals(fileEntity.getFileId(), foundFile.getFileId());
    }

    @Test
    void testGetFile_NotFound() {
        when(fileDao.findById(fileId)).thenReturn(Optional.empty());

        FileEntity foundFile = fileService.getFile(fileId);

        assertNull(foundFile);
    }

    @Test
    void testGetFilesByAgentAndLead_Success() {
        when(fileDao.findByAgentIdAndLeadId(any(), any())).thenReturn(List.of(fileEntity));

        List<FileEntity> files = fileService.getFilesByAgentAndLead("agent1", "lead1");

        assertNotNull(files);
        assertEquals(1, files.size());
        assertEquals("document.pdf", files.get(0).getOriginalFilename());
    }

    @Test
    void testGetFilesByAgentAndLead_Empty() {
        when(fileDao.findByAgentIdAndLeadId(any(), any())).thenReturn(Collections.emptyList());

        List<FileEntity> files = fileService.getFilesByAgentAndLead("agent1", "lead1");

        assertNotNull(files);
        assertTrue(files.isEmpty());
    }
}
