package com.cars24.fraud_detection.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.impl.DocumentDaoImpl;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.repository.DocumentRepository;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.service.impl.DocumentServiceImpl;
import com.cars24.fraud_detection.workflow.impl.AudioWorkflow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@ContextConfiguration(classes = {DocumentController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class DocumentControllerDiffblueTest {
    @Autowired
    private DocumentController documentController;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentTypeConfig documentTypeConfig;

    /**
     * Test {@link DocumentController#getDocument(String)}.
     * <p>
     * Method under test: {@link DocumentController#getDocument(String)}
     */
    @Test
    @DisplayName("Test getDocument(String)")
    @Tag("MaintainedByDiffblue")
    void testGetDocument() throws Exception {
        // Arrange
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
        when(documentService.getDocumentById(Mockito.<String>any())).thenReturn(buildResult);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/documents/result")
                .param("documentId", "foo");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(documentController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"documentId\":\"42\",\"finalRiskScore\":10.0,\"riskLevel\":\"Risk Level\",\"decision\":\"Decision\",\"nextSteps\":\"Next"
                                        + " Steps\",\"remarks\":\"Remarks\",\"ocrResults\":null,\"qualityResults\":null,\"forgeryResults\":null,\"validationResults"
                                        + "\":null,\"valid\":false}"));
    }

    /**
     * Test {@link DocumentController#processDocument(MultipartFile)}.
     * <ul>
     *   <li>Then StatusCode return {@link HttpStatus}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DocumentController#processDocument(MultipartFile)}
     */
    @Test
    @DisplayName("Test processDocument(MultipartFile); then StatusCode return HttpStatus")
    @Tag("MaintainedByDiffblue")
    void testProcessDocument_thenStatusCodeReturnHttpStatus() throws IOException {
        //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
        //   Run dcover create --keep-partial-tests to gain insights into why
        //   a non-Spring test was created.

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
        DocumentRepository documentRepo = mock(DocumentRepository.class);
        when(documentRepo.save(Mockito.<DocumentEntity>any())).thenReturn(documentEntity);
        DocumentDaoImpl documentDao = new DocumentDaoImpl(documentRepo);
        AudioWorkflow workflowInitiator = mock(AudioWorkflow.class);
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

        when(documentTypeConfig.getMapping()).thenReturn(Map.of(
                "AADHAR", "Aadhar",
                "ATTESTED_PAN", "Pan",
                "UNATTESTED_PAN", "Pan"
        ));

        DocumentController documentController = new DocumentController(
                new DocumentServiceImpl(documentDao, workflowInitiator, documentTypeConfig), documentTypeConfig);


        // Act
        ResponseEntity<DocumentResponse> actualProcessDocumentResult = documentController
                .processDocument(new MockMultipartFile("Name", new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"))));

        // Assert
        verify(workflowInitiator).processDocument(isA(DocumentRequest.class));
        verify(documentRepo).save(isA(DocumentEntity.class));
        HttpStatusCode statusCode = actualProcessDocumentResult.getStatusCode();
        assertTrue(statusCode instanceof HttpStatus);
        DocumentResponse body = actualProcessDocumentResult.getBody();
        assertEquals("42", body.getDocumentId());
        assertEquals("Decision", body.getDecision());
        assertEquals("Next Steps", body.getNextSteps());
        assertEquals("Remarks", body.getRemarks());
        assertEquals("Risk Level", body.getRiskLevel());
        assertNull(body.getForgeryResults());
        assertNull(body.getOcrResults());
        assertNull(body.getQualityResults());
        assertNull(body.getValidationResults());
        assertEquals(10.0d, body.getFinalRiskScore());
        assertEquals(200, actualProcessDocumentResult.getStatusCodeValue());
        assertEquals(HttpStatus.OK, statusCode);
        assertFalse(body.isValid());
        assertTrue(actualProcessDocumentResult.hasBody());
        assertTrue(actualProcessDocumentResult.getHeaders().isEmpty());
    }
}
