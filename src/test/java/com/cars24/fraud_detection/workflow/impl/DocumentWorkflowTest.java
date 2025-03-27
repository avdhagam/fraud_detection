package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.FileUtils;
import com.cars24.fraud_detection.utils.PythonExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowTest {

    @Mock
    private PythonExecutor pythonExecutor;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private DocumentWorkflow documentWorkflow;

    private DocumentRequest documentRequest;

    @BeforeEach
    void setUp() {
        documentRequest = new DocumentRequest();
        documentRequest.setAgentId("agent123");
        documentRequest.setLeadId("lead456");
        documentRequest.setDocumentType("Aadhaar");
        documentRequest.setFileName("test_document.jpg");
        documentRequest.setDocumentData(new byte[]{1, 2, 3}); // Dummy byte array
    }

    @Test
    void testProcessDocument_Success() throws Exception {
        String documentPath = "src/main/resources/document_storage/test_document.jpg";
        String ocrJsonPath = "src/main/resources/document_storage/test_document_ocr.json";

        // Mock successful file operations
        when(fileUtils.saveDocument(any(), anyString())).thenReturn(documentPath);
        when(fileUtils.executeOcr(any(), eq(documentPath))).thenReturn(Map.of("ocr_json_path", ocrJsonPath));
        when(fileUtils.extractOcrJsonPath(any())).thenReturn(ocrJsonPath);
        when(fileUtils.executePythonTask(any(), anyString(), any(), anyString())).thenReturn(Collections.emptyMap());
        when(fileUtils.computeRiskScore(any(), any(), any())).thenReturn(0.75);
        when(fileUtils.createResponse(anyString(), anyString(), anyString(), anyDouble(), any(), any(), any(), any()))
                .thenReturn(DocumentResponse.builder()
                        .documentId(UUID.randomUUID().toString())
                        .leadId("lead456")
                        .documentType("Aadhaar")
                        .finalRiskScore(0.75)
                        .isValid(true)
                        .build());

        DocumentResponse response = documentWorkflow.processDocument(documentRequest);

        assertNotNull(response);
        assertEquals("Aadhaar", response.getDocumentType());
        assertEquals(0.75, response.getFinalRiskScore());
        assertTrue(response.isValid());
        verify(fileUtils).saveDocument(any(), anyString());
    }

    @Test
    void testProcessDocument_OcrFailure() throws Exception {
        when(fileUtils.saveDocument(any(), anyString())).thenReturn("dummy/path");
        when(fileUtils.executeOcr(any(), anyString())).thenReturn(Collections.emptyMap());

        Exception exception = assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(documentRequest));
        assertTrue(exception.getMessage().contains("OCR JSON path not found"));
    }

    @Test
    void testProcessDocument_ValidationFailure() throws Exception {
        when(fileUtils.saveDocument(any(), anyString())).thenReturn("dummy/path");
        when(fileUtils.executeOcr(any(), anyString())).thenReturn(Map.of("ocr_json_path", "dummy/ocr.json"));
        when(fileUtils.extractOcrJsonPath(any())).thenReturn("dummy/ocr.json");
        when(fileUtils.executePythonTask(any(), anyString(), any(), anyString())).thenReturn(Collections.emptyMap());

        doThrow(new DocumentProcessingException("Validation failed"))
                .when(fileUtils).executePythonTask(any(), eq("src/main/resources/python_workflows/DocumentValidation.py"), any(), anyString());

        Exception exception = assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(documentRequest));
        assertTrue(exception.getMessage().contains("Validation failed"));
    }

    @Test
    void testProcessDocument_FileSaveFailure() throws Exception {
        when(fileUtils.saveDocument(any(), anyString())).thenThrow(new RuntimeException("File save error"));

        Exception exception = assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(documentRequest));
        assertTrue(exception.getMessage().contains("Document processing failed"));
    }

    @Test
    void testProcessDocument_GeneralFailure() throws Exception {
        when(fileUtils.saveDocument(any(), anyString())).thenReturn("dummy/path");
        when(fileUtils.executeOcr(any(), anyString())).thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(documentRequest));
        assertTrue(exception.getMessage().contains("Document processing failed"));
    }
}
