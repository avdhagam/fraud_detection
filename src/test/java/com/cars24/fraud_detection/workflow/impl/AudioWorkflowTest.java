package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioWorkflowTest {

    @Mock
    private PythonExecutor pythonExecutor;

    @InjectMocks
    private AudioWorkflow audioWorkflow;

    private AudioRequest audioRequest;
    private Map<String, Object> mockLlmResponse;
    private Map<String, Object> mockAudioAnalysis;

    @BeforeEach
    void setUp() {
        audioRequest = new AudioRequest();
        audioRequest.setUuid("test-audio-uuid");
        audioRequest.setAgentId("agent123");
        audioRequest.setLeadId("lead456");

        // Mock LLM Extraction Output
        mockLlmResponse = new HashMap<>();
        mockLlmResponse.put("status", "completed");

        Map<String, Object> extractedResult = new HashMap<>();
        extractedResult.put("reference_name", "John Doe");
        extractedResult.put("subject_name", "Jane Doe");
        extractedResult.put("subject_address", "123 Main St");
        extractedResult.put("relation_to_subject", "Spouse");
        extractedResult.put("subject_occupation", "Engineer");

        mockLlmResponse.put("extracted_result", extractedResult);
        mockLlmResponse.put("transcript", List.of(Map.of("speaker", "Speaker1", "text", "Hello")));
        mockLlmResponse.put("scoring_results", Map.of("overall_score", 0.85));

        // Mock Audio Analysis Output
        mockAudioAnalysis = new HashMap<>();
        mockAudioAnalysis.put("noise_level", "low");
        mockAudioAnalysis.put("clarity", "high");
    }

    @Test
    void testProcessAudio_Success() throws JsonProcessingException {
        when(pythonExecutor.runPythonScript(anyString(), anyString(), anyString()))
                .thenReturn(mockLlmResponse)
                .thenReturn(mockAudioAnalysis); // Two calls: LLM & Audio Analysis

        AudioResponse response = audioWorkflow.processAudio(audioRequest);

        assertNotNull(response);
        assertEquals("completed", response.getStatus());
        assertEquals("John Doe", response.getReferenceName());
        assertEquals("Jane Doe", response.getSubjectName());
        assertEquals("123 Main St", response.getSubjectAddress());
        assertEquals("Spouse", response.getRelationToSubject());
        assertEquals("Engineer", response.getSubjectOccupation());
        assertEquals(0.85, response.getOverallScore());
        assertEquals(mockAudioAnalysis, response.getAudioAnalysis());
        assertFalse(response.getTranscript().isEmpty());

        verify(pythonExecutor, times(2)).runPythonScript(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessAudio_EmptyResponse() throws JsonProcessingException {
        when(pythonExecutor.runPythonScript(anyString(), anyString(), anyString())).thenReturn(Collections.emptyMap());

        AudioResponse response = audioWorkflow.processAudio(audioRequest);

        assertNotNull(response);
        assertEquals("error", response.getStatus());
        assertNull(response.getReferenceName());
        assertTrue(response.getTranscript().isEmpty());

        verify(pythonExecutor, times(1)).runPythonScript(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessAudio_ExceptionHandling() throws JsonProcessingException {
        when(pythonExecutor.runPythonScript(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Script execution failed"));

        assertThrows(RuntimeException.class, () -> audioWorkflow.processAudio(audioRequest));

        verify(pythonExecutor, times(1)).runPythonScript(anyString(), anyString(), anyString());
    }
}
