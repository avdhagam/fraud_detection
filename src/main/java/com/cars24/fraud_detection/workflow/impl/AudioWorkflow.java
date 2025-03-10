package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@Primary
public class AudioWorkflow implements WorkflowInitiator {

    @Value("${python.script.llm-extraction:src/main/resources/python_workflows/LLMextractionvalidation.py}")
    private String llmScriptPath;

    @Value("${python.script.audio-analysis:src/main/resources/python_workflows/AudioAnalysis.py}")
    private String analysisScriptPath;

    private final ObjectMapper objectMapper;
    private final PythonExecutor pythonExecutor;

    @Autowired
    public AudioWorkflow(ObjectMapper objectMapper, PythonExecutor pythonExecutor) {
        this.objectMapper = objectMapper;
        this.pythonExecutor = pythonExecutor;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        // Not implemented in this class
        log.warn("Document processing not implemented in AudioWorkflow");
        return null;
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws AudioProcessingException {
        validateRequest(request);
        String requestId = request.getUuid();

        log.info("Starting audio processing for requestId: {}", requestId);

        try {
            // Execute both Python scripts in parallel
            CompletableFuture<Map<String, Object>> llmExtractionFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return executePythonScript(llmScriptPath, requestId);
                        } catch (AudioProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

            CompletableFuture<Map<String, Object>> audioAnalysisFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return executePythonScript(analysisScriptPath, requestId);
                        } catch (AudioProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Wait for LLM extraction to complete
            Map<String, Object> llmExtractionResult = llmExtractionFuture.get();
            JsonNode rootNode = parseJson(llmExtractionResult.get("output"));

            if (rootNode == null) {
                throw new AudioProcessingException("Invalid LLM extraction result for requestId: " + requestId);
            }

            // Create response from LLM extraction results
            AudioResponse response = extractAudioResponse(rootNode, requestId);
            response.setLlmExtraction(llmExtractionResult);

            // Wait for audio analysis to complete
            Map<String, Object> audioAnalysisResult = audioAnalysisFuture.get();
            response.setAudioAnalysis(audioAnalysisResult);

            log.info("Audio processing completed successfully for requestId: {}", requestId);
            return response;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AudioProcessingException) {
                throw (AudioProcessingException) cause;
            }
            log.error("ExecutionException processing audio for requestId: {}", requestId, e);
            throw new AudioProcessingException("Audio processing failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while processing audio for requestId: {}", requestId, e);
            throw new AudioProcessingException("Audio processing interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error processing audio for requestId: {}", requestId, e);
            throw new AudioProcessingException("Audio processing failed: " + e.getMessage(), e);
        }
    }

    private void validateRequest(AudioRequest request) throws AudioProcessingException {
        if (request == null) {
            throw new AudioProcessingException("Audio request cannot be null");
        }

        if (!StringUtils.hasText(request.getUuid())) {
            throw new AudioProcessingException("Request UUID is required");
        }

        if (!StringUtils.hasText(request.getFilepath())) {
            throw new AudioProcessingException("Audio file path is required");
        }
    }

    private Map<String, Object> executePythonScript(String scriptPath, String uuid) throws AudioProcessingException {
        try {
            log.info("Executing Python script: {} for requestId: {}", scriptPath, uuid);
            Map<String, Object> result = pythonExecutor.runPythonScript(scriptPath, uuid);

            if (result == null || result.isEmpty()) {
                throw new AudioProcessingException("Python script returned empty response");
            }

            return result;
        } catch (Exception e) {
            log.error("Error executing Python script: {} for requestId: {}", scriptPath, uuid, e);
            throw new AudioProcessingException("Python script execution failed: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJson(Object jsonData) throws AudioProcessingException {
        try {
            return jsonData != null ? objectMapper.readTree(jsonData.toString()) : null;
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON data", e);
            throw new AudioProcessingException("JSON parsing failed: " + e.getMessage(), e);
        }
    }

    private AudioResponse extractAudioResponse(JsonNode rootNode, String uuid) throws AudioProcessingException {
        try {
            JsonNode extractedResult = rootNode.path("extracted_result");
            JsonNode scoringResults = rootNode.path("scoring_results");

            AudioResponse response = new AudioResponse();
            response.setUuid(uuid);
            response.setReferenceName(getNodeTextValue(extractedResult, "reference_name"));
            response.setSubjectName(getNodeTextValue(extractedResult, "subject_name"));
            response.setSubjectAddress(getNodeTextValue(extractedResult, "subject_address"));
            response.setRelationToSubject(getNodeTextValue(extractedResult, "relation_to_subject"));
            response.setSubjectOccupation(getNodeTextValue(extractedResult, "subject_occupation"));
            response.setOverallScore(scoringResults.path("overall_score").asDouble(0.0));
            response.setTranscript(extractTranscript(rootNode.path("transcript")));
            response.setExplanation(extractExplanations(scoringResults.path("explanation")));
            response.setFieldByFieldScores(extractFieldScores(scoringResults.path("field_by_field_scores")));

            return response;
        } catch (Exception e) {
            log.error("Error extracting audio response for requestId: {}", uuid, e);
            throw new AudioProcessingException("Error extracting audio response: " + e.getMessage(), e);
        }
    }

    private String getNodeTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.path(field);
        return fieldNode.isMissingNode() || fieldNode.isNull() ? "" : fieldNode.asText("");
    }

    private List<String> extractTranscript(JsonNode transcriptNode) {
        if (transcriptNode == null || transcriptNode.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> transcriptList = new ArrayList<>();
        for (JsonNode entry : transcriptNode) {
            transcriptList.add(String.format("Speaker: %s | Start: %.2f | End: %.2f | Text: %s",
                    entry.path("speaker").asText("Unknown"),
                    entry.path("start_time").asDouble(0.0),
                    entry.path("end_time").asDouble(0.0),
                    entry.path("text").asText("")));
        }
        return transcriptList;
    }

    private List<String> extractExplanations(JsonNode explanationNode) {
        if (explanationNode == null || explanationNode.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> explanations = new ArrayList<>();
        explanationNode.fields().forEachRemaining(field ->
                explanations.add(field.getKey() + ": " + field.getValue().asText("")));
        return explanations;
    }

    private Map<String, Double> extractFieldScores(JsonNode fieldScoresNode) {
        if (fieldScoresNode == null || fieldScoresNode.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> fieldScores = new HashMap<>();
        fieldScoresNode.fields().forEachRemaining(field ->
                fieldScores.put(field.getKey(), field.getValue().asDouble(0.0)));
        return fieldScores;
    }
}