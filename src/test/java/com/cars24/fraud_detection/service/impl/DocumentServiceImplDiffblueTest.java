package com.cars24.fraud_detection.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {DocumentServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class DocumentServiceImplDiffblueTest {
    @MockBean
    private DocumentDao documentDao;

    @Autowired
    private DocumentServiceImpl documentServiceImpl;

    @MockBean(name = "documentWorkflow")
    private WorkflowInitiator workflowInitiator;

    /**
     * Test {@link DocumentServiceImpl#processDocument(DocumentRequest)}.
     * <ul>
     *   <li>Then return DocumentId is {@code 42}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentServiceImpl#processDocument(DocumentRequest)}
     */
    @Test
    @DisplayName("Test processDocument(DocumentRequest); then return DocumentId is '42'")
    @Tag("MaintainedByDiffblue")
    void testProcessDocument_thenReturnDocumentIdIs42() throws UnsupportedEncodingException {
        // Arrange
        doNothing().when(documentDao).saveDocument(Mockito.<DocumentEntity>any());
        DocumentResponse buildResult = DocumentResponse.builder()
                .decision("Decision")
                .documentId("42")
                .finalRiskScore(10.0d)
                .forgeryResults(null)
                .nextSteps("Next Steps")
                .ocrResults(null)
                .qualityResults(null)
                .remarks("Remarks")
                .riskLevel("Risk Level")
                .validationResults(null)
                .build();
        when(workflowInitiator.processDocument(Mockito.<DocumentRequest>any())).thenReturn(buildResult);

        DocumentRequest request = new DocumentRequest();
        request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
        request.setFileName("foo.txt");
        request.setUserId("42");

        // Act
        DocumentResponse actualProcessDocumentResult = documentServiceImpl.processDocument(request);

        // Assert
        verify(documentDao).saveDocument(isA(DocumentEntity.class));
        verify(workflowInitiator).processDocument(isA(DocumentRequest.class));
        assertEquals("42", actualProcessDocumentResult.getDocumentId());
        assertEquals("Decision", actualProcessDocumentResult.getDecision());
        assertEquals("Next Steps", actualProcessDocumentResult.getNextSteps());
        assertEquals("Remarks", actualProcessDocumentResult.getRemarks());
        assertEquals("Risk Level", actualProcessDocumentResult.getRiskLevel());
        assertNull(actualProcessDocumentResult.getForgeryResults());
        assertNull(actualProcessDocumentResult.getOcrResults());
        assertNull(actualProcessDocumentResult.getQualityResults());
        assertNull(actualProcessDocumentResult.getValidationResults());
        assertEquals(10.0d, actualProcessDocumentResult.getFinalRiskScore());
        assertFalse(actualProcessDocumentResult.isValid());
    }

    /**
     * Test {@link DocumentServiceImpl#processDocument(DocumentRequest)}.
     * <ul>
     *   <li>Then throw {@link DocumentProcessingException}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentServiceImpl#processDocument(DocumentRequest)}
     */
    @Test
    @DisplayName("Test processDocument(DocumentRequest); then throw DocumentProcessingException")
    @Tag("MaintainedByDiffblue")
    void testProcessDocument_thenThrowDocumentProcessingException() throws UnsupportedEncodingException {
        // Arrange
        doThrow(new DocumentProcessingException("An error occurred")).when(documentDao)
                .saveDocument(Mockito.<DocumentEntity>any());
        DocumentResponse buildResult = DocumentResponse.builder()
                .decision("Decision")
                .documentId("42")
                .finalRiskScore(10.0d)
                .forgeryResults(null)
                .nextSteps("Next Steps")
                .ocrResults(null)
                .qualityResults(null)
                .remarks("Remarks")
                .riskLevel("Risk Level")
                .validationResults(null)
                .build();
        when(workflowInitiator.processDocument(Mockito.<DocumentRequest>any())).thenReturn(buildResult);

        DocumentRequest request = new DocumentRequest();
        request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
        request.setFileName("foo.txt");
        request.setUserId("42");

        // Act and Assert
        assertThrows(DocumentProcessingException.class, () -> documentServiceImpl.processDocument(request));
        verify(documentDao).saveDocument(isA(DocumentEntity.class));
        verify(workflowInitiator).processDocument(isA(DocumentRequest.class));
    }

    /**
     * Test {@link DocumentServiceImpl#getDocumentById(String)}.
     * <ul>
     *   <li>Given {@link DocumentEntity#DocumentEntity()} Decision is {@code Decision}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentServiceImpl#getDocumentById(String)}
     */
    @Test
    @DisplayName("Test getDocumentById(String); given DocumentEntity() Decision is 'Decision'")
    @Tag("MaintainedByDiffblue")
    void testGetDocumentById_givenDocumentEntityDecisionIsDecision() {
        // Arrange
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDecision("Decision");
        documentEntity.setFileName("foo.txt");
        documentEntity.setFilePath("/directory/foo.txt");
        documentEntity.setFinalRiskScore(10.0d);
        documentEntity.setId("42");
        documentEntity.setNextSteps("Next Steps");
        documentEntity.setRemarks("Remarks");
        documentEntity.setRiskLevel("Risk Level");
        documentEntity.setStatus("Status");
        documentEntity.setUserId("42");
        Optional<DocumentEntity> ofResult = Optional.of(documentEntity);
        when(documentDao.getDocumentById(Mockito.<String>any())).thenReturn(ofResult);

        // Act
        DocumentResponse actualDocumentById = documentServiceImpl.getDocumentById("42");

        // Assert
        verify(documentDao).getDocumentById(eq("42"));
        assertEquals("42", actualDocumentById.getDocumentId());
        assertEquals("Decision", actualDocumentById.getDecision());
        assertEquals("Next Steps", actualDocumentById.getNextSteps());
        assertEquals("Remarks", actualDocumentById.getRemarks());
        assertEquals("Risk Level", actualDocumentById.getRiskLevel());
        assertNull(actualDocumentById.getForgeryResults());
        assertNull(actualDocumentById.getOcrResults());
        assertNull(actualDocumentById.getQualityResults());
        assertNull(actualDocumentById.getValidationResults());
        assertEquals(10.0d, actualDocumentById.getFinalRiskScore());
        assertFalse(actualDocumentById.isValid());
    }

    /**
     * Test {@link DocumentServiceImpl#getDocumentById(String)}.
     * <ul>
     *   <li>Then calls {@link DocumentEntity#setDecision(String)}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentServiceImpl#getDocumentById(String)}
     */
    @Test
    @DisplayName("Test getDocumentById(String); then calls setDecision(String)")
    @Tag("MaintainedByDiffblue")
    void testGetDocumentById_thenCallsSetDecision() {
        // Arrange
        DocumentEntity documentEntity = mock(DocumentEntity.class);
        DocumentResponse buildResult = DocumentResponse.builder()
                .decision("Decision")
                .documentId("42")
                .finalRiskScore(10.0d)
                .forgeryResults(null)
                .nextSteps("Next Steps")
                .ocrResults(null)
                .qualityResults(null)
                .remarks("Remarks")
                .riskLevel("Risk Level")
                .validationResults(null)
                .build();
        when(documentEntity.toResponse()).thenReturn(buildResult);
        doNothing().when(documentEntity).setDecision(Mockito.<String>any());
        doNothing().when(documentEntity).setFileName(Mockito.<String>any());
        doNothing().when(documentEntity).setFilePath(Mockito.<String>any());
        doNothing().when(documentEntity).setFinalRiskScore(anyDouble());
        doNothing().when(documentEntity).setId(Mockito.<String>any());
        doNothing().when(documentEntity).setNextSteps(Mockito.<String>any());
        doNothing().when(documentEntity).setRemarks(Mockito.<String>any());
        doNothing().when(documentEntity).setRiskLevel(Mockito.<String>any());
        doNothing().when(documentEntity).setStatus(Mockito.<String>any());
        doNothing().when(documentEntity).setUserId(Mockito.<String>any());
        documentEntity.setDecision("Decision");
        documentEntity.setFileName("foo.txt");
        documentEntity.setFilePath("/directory/foo.txt");
        documentEntity.setFinalRiskScore(10.0d);
        documentEntity.setId("42");
        documentEntity.setNextSteps("Next Steps");
        documentEntity.setRemarks("Remarks");
        documentEntity.setRiskLevel("Risk Level");
        documentEntity.setStatus("Status");
        documentEntity.setUserId("42");
        Optional<DocumentEntity> ofResult = Optional.of(documentEntity);
        when(documentDao.getDocumentById(Mockito.<String>any())).thenReturn(ofResult);

        // Act
        DocumentResponse actualDocumentById = documentServiceImpl.getDocumentById("42");

        // Assert
        verify(documentDao).getDocumentById(eq("42"));
        verify(documentEntity).setDecision(eq("Decision"));
        verify(documentEntity).setFileName(eq("foo.txt"));
        verify(documentEntity).setFilePath(eq("/directory/foo.txt"));
        verify(documentEntity).setFinalRiskScore(eq(10.0d));
        verify(documentEntity).setId(eq("42"));
        verify(documentEntity).setNextSteps(eq("Next Steps"));
        verify(documentEntity).setRemarks(eq("Remarks"));
        verify(documentEntity).setRiskLevel(eq("Risk Level"));
        verify(documentEntity).setStatus(eq("Status"));
        verify(documentEntity).setUserId(eq("42"));
        verify(documentEntity).toResponse();
        assertEquals("42", actualDocumentById.getDocumentId());
        assertEquals("Decision", actualDocumentById.getDecision());
        assertEquals("Next Steps", actualDocumentById.getNextSteps());
        assertEquals("Remarks", actualDocumentById.getRemarks());
        assertEquals("Risk Level", actualDocumentById.getRiskLevel());
        assertNull(actualDocumentById.getForgeryResults());
        assertNull(actualDocumentById.getOcrResults());
        assertNull(actualDocumentById.getQualityResults());
        assertNull(actualDocumentById.getValidationResults());
        assertEquals(10.0d, actualDocumentById.getFinalRiskScore());
        assertFalse(actualDocumentById.isValid());
    }

    /**
     * Test {@link DocumentServiceImpl#getDocumentById(String)}.
     * <ul>
     *   <li>Then throw {@link DocumentProcessingException}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentServiceImpl#getDocumentById(String)}
     */
    @Test
    @DisplayName("Test getDocumentById(String); then throw DocumentProcessingException")
    @Tag("MaintainedByDiffblue")
    void testGetDocumentById_thenThrowDocumentProcessingException() {
        // Arrange
        Optional<DocumentEntity> emptyResult = Optional.empty();
        when(documentDao.getDocumentById(Mockito.<String>any())).thenReturn(emptyResult);

        // Act and Assert
        assertThrows(DocumentProcessingException.class, () -> documentServiceImpl.getDocumentById("42"));
        verify(documentDao).getDocumentById(eq("42"));
    }
}
