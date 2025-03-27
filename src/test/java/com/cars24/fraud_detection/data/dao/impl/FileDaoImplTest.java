package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDaoImplTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileDaoImpl fileDao;

    private FileEntity fileEntity;

    @BeforeEach
    void setUp() {
        fileEntity = new FileEntity("file123", "agent1", "lead1", "document.pdf", "pdf");
    }

    @Test
    void testSave() {
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);

        FileEntity savedFile = fileDao.save(fileEntity);

        assertNotNull(savedFile);
        assertEquals("file123", savedFile.getFileId());
        verify(fileRepository).save(fileEntity);
    }

    @Test
    void testFindById() {
        when(fileRepository.findById("file123")).thenReturn(Optional.of(fileEntity));

        Optional<FileEntity> foundFile = fileDao.findById("file123");

        assertTrue(foundFile.isPresent());
        assertEquals("file123", foundFile.get().getFileId());
    }
}
