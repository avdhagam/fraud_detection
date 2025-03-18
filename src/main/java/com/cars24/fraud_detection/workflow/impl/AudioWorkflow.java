package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.utils.AudioStringConstants;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.utils.PythonExecutor2;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired; // Add this import

import java.util.*;
@Slf4j
@Service
@Primary
public class AudioWorkflow implements WorkflowInitiator {

    private static final Logger logger = LoggerFactory.getLogger(AudioWorkflow.class);
    private final PythonExecutor2 pythonExecutor2;

    @Autowired
    public AudioWorkflow(PythonExecutor2 pythonExecutor2) {
        this.pythonExecutor2 = pythonExecutor2;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        return null;
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        logger.info("Starting audio processing for requestId: {}", request.getUuid());

        // Run the LLMextractionvalidation.py script and get the result
        String llmScriptPath = "src/main/resources/python_workflows/LLMextractionvalidation.py";
        logger.info("Executing LLM extraction script: {} for audio file: {}", llmScriptPath, request.getAudioFile());

        Map<String, Object> llmExtractionResult = processAudioScript(llmScriptPath, request.getUuid());

        Object obj = llmExtractionResult.get("output");

        logger.debug("Raw output from Python script: {}", obj);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode rootNode = objectMapper.readTree(obj.toString());

        // Extract values from extracted_result
        JsonNode extractedResult = rootNode.get(AudioStringConstants.EXTRACTED_RESULT);
        String referenceName = extractedResult.get(AudioStringConstants.REFERENCE_NAME).asText();
        String subjectName = extractedResult.get(AudioStringConstants.SUBJECT_NAME).asText();
        String subjectAddress = extractedResult.get(AudioStringConstants.SUBJECT_ADDRESS).asText();
        String relationToSubject = extractedResult.get(AudioStringConstants.RELATION_TO_SUBJECT).asText();
        String subjectOccupation = extractedResult.get(AudioStringConstants.SUBJECT_OCCUPATION).asText();

        double overallScore = rootNode.get(AudioStringConstants.SCORING_RESULTS).get("overall_score").asDouble();
        String status = rootNode.has(AudioStringConstants.STATUS) ?
                rootNode.get(AudioStringConstants.STATUS).asText() :
                (overallScore >= 0.7 ? "accept" : "reject");

            // Extract transcript
            List<String> transcriptList = new ArrayList<>();
            JsonNode transcriptNode = rootNode.path(AudioStringConstants.TRANSCRIPT);
            for (JsonNode entry : transcriptNode) {
                transcriptList.add(entry.path("text").asText());
            }

            // Extract explanations
            JsonNode explanationNode = rootNode.path(AudioStringConstants.SCORING_RESULTS).path(AudioStringConstants.EXPLANATION);
            List<String> explanations = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = explanationNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                explanations.add(field.getKey() + ": " + field.getValue().asText());
            }

            JsonNode fieldScoresNode = rootNode.path(AudioStringConstants.SCORING_RESULTS).path(AudioStringConstants.FIELD_BY_FIELD_SCORES);
            Map<String, Double> fieldScores = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> scoreFields = fieldScoresNode.fields();
            while (scoreFields.hasNext()) {
                Map.Entry<String, JsonNode> field = scoreFields.next();
                fieldScores.put(field.getKey(), field.getValue().asDouble());
            }


            // Store extracted values (Example: storing in a map)
            Map<String, Object> extractedData = new HashMap<>();
            extractedData.put("reference_name", referenceName);
            extractedData.put("subject_name", subjectName);
            extractedData.put("subject_address", subjectAddress);
            extractedData.put("relation_to_subject", relationToSubject);
            extractedData.put("subject_occupation", subjectOccupation);
            extractedData.put("overall_score", overallScore);
            extractedData.put("transcript", transcriptList);
            extractedData.put("explanation", explanations);
            extractedData.put("field_by_field_scores", fieldScores);
            extractedData.put("status",status);

            // Log extracted values
            log.info("Extracted Data: {}", extractedData);



        if (llmExtractionResult == null || llmExtractionResult.isEmpty()) {
            logger.warn("LLM extraction script returned an empty response for requestId: {}", request.getUuid());
        } else {
            logger.info("LLM extraction completed successfully for requestId: {}", request.getUuid());
        }

        // Create AudioResponse
        AudioResponse response = new AudioResponse();
        response.setUuid(request.getUuid());
        //response.setLlmExtraction(llmExtractionResult);
        response.setStatus((String) extractedData.get("status"));
        response.setTranscript((List<String>) extractedData.get("transcript"));
        response.setReferenceName((String) extractedData.get("reference_name"));
        response.setSubjectName((String) extractedData.get("subject_name"));
        response.setSubjectAddress((String) extractedData.get("subject_address"));
        response.setRelationToSubject((String) extractedData.get("relation_to_subject"));
        response.setSubjectOccupation((String) extractedData.get("subject_occupation"));
        response.setOverallScore((Double) extractedData.get("overall_score"));
        response.setExplanation((List<String>) extractedData.get("explanation"));
        response.setFieldByFieldScores((Map<String, Double>) extractedData.get("field_by_field_scores"));

        // Run the audio analysis script and get the result
        String analysisScriptPath = "src/main/resources/python_workflows/AudioAnalysis.py";
        logger.info("Executing audio analysis script: {} for audio file: {}", analysisScriptPath, request.getAudioFile());

        Map<String, Object> audioAnalysisResult = processAudioScript(analysisScriptPath, request.getUuid());

        if (audioAnalysisResult == null || audioAnalysisResult.isEmpty()) {
            logger.warn("Audio analysis script returned an empty response for requestId: {}", request.getUuid());
        } else {
            logger.info("Audio analysis completed successfully for requestId: {}", request.getUuid());
        }

        response.setAudioAnalysis(audioAnalysisResult);
        logger.info("Audio processing completed for requestId: {}", request.getUuid());

        return response;
    }

    private Map<String, Object> processAudioScript(String scriptPath, String uuid) {
        logger.debug("Running Python script: {} with audio file: {}", scriptPath, uuid);
        Map<String, Object> result = pythonExecutor2.runPythonScript(scriptPath, uuid);

        if (result == null || result.isEmpty()) {
            logger.warn("Python script {} returned an empty result for audio file: {}", scriptPath, uuid);
        } else {
            logger.info("Python script {} execution successful", scriptPath);
        }

        return result;
    }
}
