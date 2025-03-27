package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDaoImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentDaoImpl documentDao;

    private DocumentEntity documentEntity;

    @BeforeEach
    void setUp() {
        documentEntity = new DocumentEntity();
        documentEntity.setId("123");
        documentEntity.setLeadId("lead1");
        documentEntity.setDocumentType("Aadhaar");
    }

    @Test
    void testSaveDocument() {
        when(documentRepository.save(any(DocumentEntity.class))).thenReturn(documentEntity);
        DocumentEntity savedDoc = documentDao.saveDocument(documentEntity);
        assertNotNull(savedDoc);
        assertEquals("123", savedDoc.getId());
    }

    @Test
    void testUpdateDocument() {
        when(documentRepository.save(any(DocumentEntity.class))).thenReturn(documentEntity);
        DocumentEntity updatedDoc = documentDao.updateDocument(documentEntity);
        assertNotNull(updatedDoc);
        assertEquals("123", updatedDoc.getId());
    }

    @Test
    void testGetDocumentById() {
        when(documentRepository.findById(anyString())).thenReturn(Optional.of(documentEntity));
        Optional<DocumentEntity> foundDoc = documentDao.getDocumentById("123");
        assertTrue(foundDoc.isPresent());
        assertEquals("123", foundDoc.get().getId());
    }

    @Test
    void testFindByLeadId() {
        when(documentRepository.findByLeadId(anyString())).thenReturn(Arrays.asList(documentEntity));
        List<DocumentEntity> docs = documentDao.findByLeadId("lead1");
        assertFalse(docs.isEmpty());
        assertEquals(1, docs.size());
    }

    @Test
    void testGetRecentDocumentsByLeadId() {
        when(documentRepository.findByLeadIdOrderByTimestampDesc(anyString(), any(PageRequest.class)))
                .thenReturn(Arrays.asList(documentEntity));
        List<DocumentEntity> docs = documentDao.getRecentDocumentsByLeadId("lead1", 5);
        assertFalse(docs.isEmpty());
        assertEquals(1, docs.size());
    }

    @Test
    void testFindByLeadIdAndDocumentType() {
        when(documentRepository.findByLeadIdAndDocumentType(anyString(), anyString()))
                .thenReturn(Arrays.asList(documentEntity));
        List<DocumentEntity> docs = documentDao.findByLeadIdAndDocumentType("lead1", "Aadhaar");
        assertFalse(docs.isEmpty());
        assertEquals("Aadhaar", docs.get(0).getDocumentType());
    }

    @Test
    void testFindByIdAndDocumentType() {
        when(documentRepository.findByIdAndDocumentType(anyString(), anyString()))
                .thenReturn(Optional.of(documentEntity));
        Optional<DocumentEntity> foundDoc = documentDao.findByIdAndDocumentType("123", "Aadhaar");
        assertTrue(foundDoc.isPresent());
        assertEquals("123", foundDoc.get().getId());
    }

    @Test
    void testGetRecentDocumentsByLeadIdAndType() {
        when(documentRepository.getRecentDocumentsByLeadIdAndType(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(Arrays.asList(documentEntity));
        List<DocumentEntity> docs = documentDao.getRecentDocumentsByLeadIdAndType("lead1", "Aadhaar", PageRequest.of(0, 5));
        assertFalse(docs.isEmpty());
        assertEquals("Aadhaar", docs.get(0).getDocumentType());
    }
}