package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentDao documentDao;

    @Mock
    private LeadDao leadDao;

    @Mock
    private WorkflowInitiator workflowInitiator;

    @Mock
    private DocumentTypeConfig documentTypeConfig;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private DocumentEntity documentEntity;
    private DocumentRequest documentRequest;
    private DocumentResponse documentResponse;

    @BeforeEach
    void setUp() {
        documentEntity = new DocumentEntity();
        documentEntity.setId("123");
        documentEntity.setLeadId("lead1");
        documentEntity.setDocumentType("Aadhaar");
        documentEntity.setFileName("document1.jpg");

        documentRequest = new DocumentRequest();
        documentRequest.setLeadId("lead1");
        documentRequest.setFileName("document1.jpg");

        documentResponse = DocumentResponse.builder()
                .documentId("123")
                .leadId("lead1")
                .documentType("Aadhaar")
                .isValid(true)
                .finalRiskScore(0.85)
                .riskLevel("Low")
                .decision("Approve")
                .nextSteps("Proceed with verification")
                .remarks("Valid document")
                .build();


    }

    @Test
    void testProcessDocument_Success() {
        when(leadDao.findLeadById(anyString())).thenReturn(Optional.of(new LeadEntity()));
        when(workflowInitiator.processDocument(any(DocumentRequest.class))).thenReturn(documentResponse);
        when(documentDao.saveDocument(any(DocumentEntity.class))).thenReturn(documentEntity);

        DocumentResponse response = documentService.processDocument(documentRequest);

        assertNotNull(response);
        assertEquals("123", response.getDocumentId());
        verify(documentDao).saveDocument(any(DocumentEntity.class));
    }

    @Test
    void testProcessDocument_LeadNotFound() {
        when(leadDao.findLeadById(anyString())).thenReturn(Optional.empty());

        assertThrows(DocumentProcessingException.class, () -> documentService.processDocument(documentRequest));
    }

    @Test
    void testGetDocumentById() {
        when(documentDao.getDocumentById(anyString())).thenReturn(Optional.of(documentEntity));

        DocumentResponse response = documentService.getDocumentById("123");

        assertNotNull(response);
        assertEquals("123", response.getDocumentId());
    }

    @Test
    void testGetDocumentById_NotFound() {
        when(documentDao.getDocumentById(anyString())).thenReturn(Optional.empty());

        assertThrows(DocumentProcessingException.class, () -> documentService.getDocumentById("123"));
    }

    @Test
    void testGetDocumentsByLeadId() {
        when(documentDao.findByLeadId(anyString())).thenReturn(Arrays.asList(documentEntity));

        List<DocumentEntity> documents = documentService.getDocumentsByLeadId("lead1");

        assertFalse(documents.isEmpty());
        assertEquals(1, documents.size());
    }

    @Test
    void testGetRecentDocumentNames() {
        when(documentDao.getRecentDocumentsByLeadId(anyString(), anyInt())).thenReturn(Arrays.asList(documentEntity));

        List<String> documentNames = documentService.getRecentDocumentNames("lead1", 5);

        assertFalse(documentNames.isEmpty());
        assertEquals("document1.jpg", documentNames.get(0));
    }

    @Test
    void testGetDocumentInsights() {
        when(documentDao.getDocumentById(anyString())).thenReturn(Optional.of(documentEntity));
        when(documentTypeConfig.getMapping()).thenReturn(Map.of("Aadhaar", "Aadhaar Card"));

        InsightsEntity insights = documentService.getDocumentInsights("123");

        assertNotNull(insights);
        assertEquals("Aadhaar", insights.getDoctype());
        assertEquals("Aadhaar Card", insights.getDocumentName());
    }

    @Test
    void testGetRecentPanId() {
        when(documentDao.getRecentDocumentsByLeadIdAndType(anyString(), eq("Pan"), any(Pageable.class)))
                .thenReturn(Arrays.asList(documentEntity));

        List<String> panIds = documentService.getRecentPanId("lead1", 1, "Pan");

        assertFalse(panIds.isEmpty());
        assertEquals("123", panIds.get(0));
    }

    @Test
    void testGetRecentAadharId() {
        when(documentDao.getRecentDocumentsByLeadIdAndType(anyString(), eq("Aadhaar"), any(Pageable.class)))
                .thenReturn(Arrays.asList(documentEntity));

        List<String> aadharIds = documentService.getRecentAadharId("lead1", 1, "Aadhaar");

        assertFalse(aadharIds.isEmpty());
        assertEquals("123", aadharIds.get(0));
    }
}
