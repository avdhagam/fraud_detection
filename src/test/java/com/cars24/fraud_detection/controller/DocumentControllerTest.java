package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void testGetDocument() throws Exception {
        DocumentResponse response = new DocumentResponse();
        response.setDocumentId("123");
        when(documentService.getDocumentById("123")).thenReturn(response);

        mockMvc.perform(get("/documents/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("123"));
    }

    @Test
    void testGetRecentDocumentNames() throws Exception {
        List<String> mockFileNames = Collections.singletonList("document1.jpg");
        when(documentService.getRecentDocumentNames("lead1", 5)).thenReturn(mockFileNames);

        mockMvc.perform(get("/documents/recent/lead1?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("document1.jpg"));
    }

    @Test
    void testUploadDocument() throws Exception {
        when(documentService.processDocument(any())).thenReturn(new DocumentResponse());

        mockMvc.perform(multipart("/documents/upload")
                        .file("file", "dummy content".getBytes())
                        .param("agentId", "agent1")
                        .param("leadId", "lead1")
                        .param("documentType", "Aadhaar")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }
}
