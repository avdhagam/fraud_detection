//package com.cars24.fraud_detection.workflow.impl;
//
//import com.cars24.fraud_detection.data.request.AudioRequest;
//import com.cars24.fraud_detection.data.request.DocumentRequest;
//import com.cars24.fraud_detection.data.response.AudioResponse;
//import com.cars24.fraud_detection.data.response.DocumentResponse;
//import com.cars24.fraud_detection.utils.PythonExecutor;
//import com.cars24.fraud_detection.workflow.WorkflowInitiator;
//import com.cars24.fraud_detection.utils.AudioStringConstants;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Slf4j
//@Service
//@Primary
//public class AudioWorkflow implements WorkflowInitiator {
//
//    private static final Logger logger = LoggerFactory.getLogger(AudioWorkflow.class);
//
//    @Override
//    public DocumentResponse processDocument(DocumentRequest request) {
//        return null;
//    }
//
//    @Override
//    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
//        logger.info("Starting audio processing for requestId: {}", request.getUuid());
//
//        String llmScriptPath = "src/main/resources/python_workflows/LLMextractionvalidation.py";
//        Map<String, Object> llmExtractionResult = runPythonScript(llmScriptPath, request.getUuid());
//
//        if (llmExtractionResult == null || llmExtractionResult.isEmpty()) {
//            logger.warn("LLM extraction script returned an empty response for requestId: {}", request.getUuid());
//            return new AudioResponse();
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode = objectMapper.readTree(llmExtractionResult.get("output").toString());
//        JsonNode extractedResult = rootNode.path("extracted_result");
//        JsonNode scoringResults = rootNode.path("scoring_results");
//
//        Map<String, Object> extractedData = new HashMap<>();
//        extractedData.put("reference_name", extractedResult.path("reference_name").asText());
//        extractedData.put("subject_name", extractedResult.path("subject_name").asText());
//        extractedData.put("subject_address", extractedResult.path("subject_address").asText());
//        extractedData.put("relation_to_subject", extractedResult.path("relation_to_subject").asText());
//        extractedData.put("subject_occupation", extractedResult.path("subject_occupation").asText());
//        extractedData.put("overall_score", scoringResults.path("overall_score").asDouble());
//        extractedData.put("status", rootNode.has("status") ? rootNode.get("status").asText() : (scoringResults.path("overall_score").asDouble() >= 0.7 ? "accept" : "reject"));
//
//        List<String> transcriptList = new ArrayList<>();
//        for (JsonNode entry : rootNode.path("transcript")) {
//            transcriptList.add(entry.path("text").asText());
//        }
//        extractedData.put("transcript", transcriptList);
//
//        List<String> explanations = new ArrayList<>();
//        for (Iterator<Map.Entry<String, JsonNode>> fields = scoringResults.path("explanation").fields(); fields.hasNext(); ) {
//            Map.Entry<String, JsonNode> field = fields.next();
//            explanations.add(field.getKey() + ": " + field.getValue().asText());
//        }
//        extractedData.put("explanation", explanations);
//
//        Map<String, Double> fieldScores = new HashMap<>();
//        for (Iterator<Map.Entry<String, JsonNode>> scoreFields = scoringResults.path("field_by_field_scores").fields(); scoreFields.hasNext(); ) {
//            Map.Entry<String, JsonNode> field = scoreFields.next();
//            fieldScores.put(field.getKey(), field.getValue().asDouble());
//        }
//        extractedData.put("field_by_field_scores", fieldScores);
//
//        logger.info("Extracted Data: {}", extractedData);
//
//        AudioResponse response = new AudioResponse();
//        response.setUuid(request.getUuid());
//        response.setLlmExtraction(llmExtractionResult);
//        response.setUserReportId(request.getUserReportId());
//        response.setStatus((String) extractedData.get("status"));
//        response.setTranscript(transcriptList);
//        response.setReferenceName((String) extractedData.get("reference_name"));
//        response.setSubjectName((String) extractedData.get("subject_name"));
//        response.setSubjectAddress((String) extractedData.get("subject_address"));
//        response.setRelationToSubject((String) extractedData.get("relation_to_subject"));
//        response.setSubjectOccupation((String) extractedData.get("subject_occupation"));
//        response.setOverallScore((Double) extractedData.get("overall_score"));
//        response.setExplanation(explanations);
//        response.setFieldByFieldScores(fieldScores);
//
//        String analysisScriptPath = "src/main/resources/python_workflows/AudioAnalysis.py";
//        Map<String, Object> audioAnalysisResult = runPythonScript(analysisScriptPath, request.getUuid());
//
//        if (audioAnalysisResult == null || audioAnalysisResult.isEmpty()) {
//            logger.warn("Audio analysis script returned an empty response for requestId: {}", request.getUuid());
//        } else {
//            logger.info("Audio analysis completed successfully for requestId: {}", request.getUuid());
//        }
//
//        response.setAudioAnalysis(audioAnalysisResult);
//        logger.info("Audio processing completed for requestId: {}", request.getUuid());
//        return response;
//    }
//
//    private Map<String, Object> runPythonScript(String scriptPath, String uuid) {
//        logger.debug("Running Python script: {} with audio file: {}", scriptPath, uuid);
//        PythonExecutor executor = new PythonExecutor();
//        Map<String, Object> result = executor.runPythonScript(scriptPath, uuid);
//
//        if (result == null || result.isEmpty()) {
//            logger.warn("Python script {} returned an empty result for audio file: {}", scriptPath, uuid);
//        } else {
//            logger.info("Python script {} execution successful", scriptPath);
//        }
//        return result;
//    }
//}

package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.utils.PythonExecutor2;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.cars24.fraud_detection.utils.AudioStringConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Primary
public class AudioWorkflow implements WorkflowInitiator {

    private static final Logger logger = LoggerFactory.getLogger(AudioWorkflow.class);

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        return null;
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        logger.info("Starting audio processing for requestId: {}", request.getUuid());

        String llmScriptPath = "src/main/resources/python_workflows/LLMextractionvalidation.py";
        Map<String, Object> llmExtractionResult = runPythonScript(llmScriptPath, request.getUuid());

        if (llmExtractionResult == null || llmExtractionResult.isEmpty()) {
            logger.warn("LLM extraction script returned an empty response for requestId: {}", request.getUuid());
            return new AudioResponse();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(llmExtractionResult.get("output").toString());
        JsonNode extractedResult = rootNode.path(AudioStringConstants.EXTRACTED_RESULT);
        JsonNode scoringResults = rootNode.path(AudioStringConstants.SCORING_RESULTS);

        Map<String, Object> extractedData = new HashMap<>();
        extractedData.put("reference_name", extractedResult.path("reference_name").asText());
        extractedData.put("subject_name", extractedResult.path("subject_name").asText());
        extractedData.put("subject_address", extractedResult.path("subject_address").asText());
        extractedData.put("relation_to_subject", extractedResult.path("relation_to_subject").asText());
        extractedData.put("subject_occupation", extractedResult.path("subject_occupation").asText());
        extractedData.put("overall_score", scoringResults.path("overall_score").asDouble());
        extractedData.put(AudioStringConstants.STATUS,
                rootNode.has(AudioStringConstants.STATUS) ? rootNode.get(AudioStringConstants.STATUS).asText() :
                        (scoringResults.path("overall_score").asDouble() >= 0.7 ? "accept" : "reject"));

        List<String> transcriptList = new ArrayList<>();
        for (JsonNode entry : rootNode.path(AudioStringConstants.TRANSCRIPT)) {
            transcriptList.add(entry.path("text").asText());
        }
        extractedData.put(AudioStringConstants.TRANSCRIPT, transcriptList);

        List<String> explanations = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> fields = scoringResults.path(AudioStringConstants.EXPLANATION).fields(); fields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = fields.next();
            explanations.add(field.getKey() + ": " + field.getValue().asText());
        }
        extractedData.put(AudioStringConstants.EXPLANATION, explanations);

        Map<String, Double> fieldScores = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> scoreFields = scoringResults.path(AudioStringConstants.FIELD_BY_FIELD_SCORES).fields(); scoreFields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = scoreFields.next();
            fieldScores.put(field.getKey(), field.getValue().asDouble());
        }
        extractedData.put(AudioStringConstants.FIELD_BY_FIELD_SCORES, fieldScores);

        logger.info("Extracted Data: {}", extractedData);

        AudioResponse response = new AudioResponse();
        response.setUuid(request.getUuid());
        response.setLlmExtraction(llmExtractionResult);
        response.setUserReportId(request.getUserReportId());
        response.setStatus((String) extractedData.get(AudioStringConstants.STATUS));
        response.setTranscript(transcriptList);
        response.setReferenceName((String) extractedData.get("reference_name"));
        response.setSubjectName((String) extractedData.get("subject_name"));
        response.setSubjectAddress((String) extractedData.get("subject_address"));
        response.setRelationToSubject((String) extractedData.get("relation_to_subject"));
        response.setSubjectOccupation((String) extractedData.get("subject_occupation"));
        response.setOverallScore((Double) extractedData.get("overall_score"));
        response.setExplanation(explanations);
        response.setFieldByFieldScores(fieldScores);

        String analysisScriptPath = "src/main/resources/python_workflows/AudioAnalysis.py";
        Map<String, Object> audioAnalysisResult = runPythonScript(analysisScriptPath, request.getUuid());

        if (audioAnalysisResult == null || audioAnalysisResult.isEmpty()) {
            logger.warn("Audio analysis script returned an empty response for requestId: {}", request.getUuid());
        } else {
            logger.info("Audio analysis completed successfully for requestId: {}", request.getUuid());
        }

        response.setAudioAnalysis(audioAnalysisResult);
        logger.info("Audio processing completed for requestId: {}", request.getUuid());
        return response;
    }

    private Map<String, Object> runPythonScript(String scriptPath, String uuid) {
        logger.debug("Running Python script: {} with audio file: {}", scriptPath, uuid);
        PythonExecutor2 executor = new PythonExecutor2();
        Map<String, Object> result = executor.runPythonScript(scriptPath, uuid);

        if (result == null || result.isEmpty()) {
            logger.warn("Python script {} returned an empty result for audio file: {}", scriptPath, uuid);
        } else {
            logger.info("Python script {} execution successful", scriptPath);
        }
        return result;
    }
}
