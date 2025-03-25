//package com.cars24.fraud_detection.workflow.impl;
//
//import com.cars24.fraud_detection.data.request.AudioRequest;
//import com.cars24.fraud_detection.data.request.DocumentRequest;
//import com.cars24.fraud_detection.data.response.AudioResponse;
//import com.cars24.fraud_detection.data.response.DocumentResponse;
//
//import com.cars24.fraud_detection.utils.PythonExecutor;
//
//import com.cars24.fraud_detection.workflow.WorkflowInitiator;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@Primary
//public class AudioWorkflow implements WorkflowInitiator {
//
//    private static final Logger logger = LoggerFactory.getLogger(AudioWorkflow.class);
//    private final PythonExecutor pythonExecutor;
//
//    @Autowired
//    public AudioWorkflow(PythonExecutor pythonExecutor) {
//        this.pythonExecutor = pythonExecutor;
//    }
//
//    @Override
//    public DocumentResponse processDocument(DocumentRequest request) {
//        return null;
//    }
//
//
//    @Override
//    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
//        logger.info("Starting audio processing for requestId: {}", request.getUuid());
//
//        // Run the LLM extraction script
//        String llmScriptPath = "src/main/resources/python_workflows/LLMextractionvalidation.py";
//        String audioScriptPath = "src/main/resources/python_workflows/AudioAnalysis.py";
//        logger.info("Executing LLM extraction script: {} for audio file: {}", llmScriptPath, request.getAudioFile());
//
//        Map<String, Object> llmExtractionResult = processAudioScript(llmScriptPath, request.getUuid());
//
//        if (llmExtractionResult == null || llmExtractionResult.isEmpty()) {
//            logger.warn("LLM extraction script returned an empty response for requestId: {}", request.getUuid());
//            return new AudioResponse(request.getUuid(), null, new ArrayList<>(), null, null, null, null, null, 0.0, new ArrayList<>(), new HashMap<>(), "error");
//        }
//
//
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        // Extract values directly from the map
//        String outputJson = (String) llmExtractionResult.get("output"); // Get JSON string
//        Map<String, Object> outputMap = objectMapper.readValue(outputJson, Map.class); // Parse JSON string
//        List<Map<String, Object>> transcriptList = (List<Map<String, Object>>) outputMap.get("transcript");
//
//        Map<String, Object> extractedResultMap = (Map<String, Object>) outputMap.get("extracted_result");
//        String referenceName = (String) extractedResultMap.get("reference_name");
//        String subjectName = (String) extractedResultMap.get("subject_name");
//        String subjectAddress = (String) extractedResultMap.get("subject_address");
//        String relationToSubject = (String) extractedResultMap.get("relation_to_subject");
//        String subjectOccupation = (String) extractedResultMap.get("subject_occupation");
//
//        Map<String,Object> audioAnalysisMap = processAudioScript(audioScriptPath, request.getUuid());
//
//
//        Map<String, Object> scoringResultsMap = (Map<String, Object>) llmExtractionResult.get("scoring_results");
//        Double overallScore = (Double) scoringResultsMap.get("overall_score");
//
//        Map<String, Object> fieldByFieldScoresMap = (Map<String, Object>) scoringResultsMap.get("field_by_field_scores");
//        Map<String, Double> fieldByFieldScores = new HashMap<>();
//        if (fieldByFieldScoresMap != null) {
//            for (Map.Entry<String, Object> entry : fieldByFieldScoresMap.entrySet()) {
//                fieldByFieldScores.put(entry.getKey(), ((Number) entry.getValue()).doubleValue()); // Cast to Number and then to double
//            }
//        }
//
//        Map<String, Object> explanationMap = (Map<String, Object>) scoringResultsMap.get("explanation");
//        List<String> explanation = new ArrayList<>();
//        if (explanationMap != null) {
//            for (Map.Entry<String, Object> entry : explanationMap.entrySet()) {
//                explanation.add(entry.getValue().toString());
//            }
//        }
//
//        String status = (String) llmExtractionResult.get("status");
//
//        logger.info("Extracted Data: {}", Arrays.asList(
//                "ReferenceName=" + referenceName,
//                "SubjectName=" + subjectName,
//                "SubjectAddress=" + subjectAddress,
//                "RelationToSubject=" + relationToSubject,
//                "SubjectOccupation=" + subjectOccupation,
//                "OverallScore=" + overallScore,
//                "FieldByFieldScores=" + fieldByFieldScores,
//                "Explanation=" + explanation,
//                "Status=" + status
//                ,"AudioAnalysis="+audioAnalysisMap
//        ));
//        // Convert transcriptList to a list of strings
//        List<String> transcript = transcriptList.stream().map(map -> map.toString()).collect(Collectors.toList());
//        // Create and return AudioResponse
//        return new AudioResponse(request.getUuid(), request.getAgentId(),llmExtractionResult, audioAnalysisMap, transcript, referenceName, subjectName, subjectAddress, relationToSubject, subjectOccupation, overallScore, explanation, fieldByFieldScores, status);
//    }
//
//    private Map<String, Object> processAudioScript(String scriptPath, String uuid) {
//        logger.debug("Running Python script: {} with audio file: {}", scriptPath, uuid);
//        Map<String, Object> result = pythonExecutor.runPythonScript(scriptPath, uuid);
//
//        if (result == null || result.isEmpty()) {
//            logger.warn("Python script {} returned an empty result for audio file: {}", scriptPath, uuid);
//        } else {
//            logger.info("Python script {} execution successful", scriptPath);
//        }
//
//        return result;
//    }
//}

package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
public class AudioWorkflow implements WorkflowInitiator {

    private static final Logger logger = LoggerFactory.getLogger(AudioWorkflow.class);
    private final PythonExecutor pythonExecutor;
    private static final String AUDIO_STORAGE_PATH = "src/main/resources/audio_storage";

    @Autowired
    public AudioWorkflow(PythonExecutor pythonExecutor) {
        this.pythonExecutor = pythonExecutor;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        return null;
    }


    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        logger.info("Starting audio processing for requestId: {}", request.getUuid());

        // Run the LLM extraction script
        String llmScriptPath = "src/main/resources/python_workflows/LLMextractionvalidation.py";
        String audioScriptPath = "src/main/resources/python_workflows/AudioAnalysis.py";
       // String audioFilePath = AUDIO_STORAGE_PATH + "/" + request.getUuid() + ".mp3";  // Construct full path
        String audioFilePath =  request.getUuid() ;
        logger.info("Executing LLM extraction script: {} for audio file: {}", llmScriptPath, request.getAudioFile());

        Map<String, Object> llmExtractionResult = processAudioScript(llmScriptPath, audioFilePath, request.getLeadId());

        if (llmExtractionResult == null || llmExtractionResult.isEmpty()) {
            logger.warn("LLM extraction script returned an empty response for requestId: {}", request.getUuid());
            return new AudioResponse(request.getUuid(), null, new ArrayList<>(), null, null, null, null, null, 0.0, new ArrayList<>(), new HashMap<>(), "error");
        }

//        ObjectMapper objectMapper = new ObjectMapper();
//        // Extract values directly from the map
//        String outputJson = (String) llmExtractionResult.get("output"); // Get JSON string
        try {
//            Map<String, Object> outputMap = objectMapper.readValue(outputJson, Map.class); // Parse JSON string
//            List<Map<String, Object>> transcriptList = (List<Map<String, Object>>) outputMap.get("transcript");
//
//            Map<String, Object> extractedResultMap = (Map<String, Object>) outputMap.get("extracted_result");

            List<Map<String, Object>> transcriptList = (List<Map<String, Object>>) llmExtractionResult.get("transcript");

            Map<String, Object> extractedResultMap = (Map<String, Object>) llmExtractionResult.get("extracted_result");

            String referenceName = (String) extractedResultMap.get("reference_name");
            String subjectName = (String) extractedResultMap.get("subject_name");
            String subjectAddress = (String) extractedResultMap.get("subject_address");
            String relationToSubject = (String) extractedResultMap.get("relation_to_subject");
            String subjectOccupation = (String) extractedResultMap.get("subject_occupation");

            Map<String,Object> audioAnalysisMap = processAudioScript(audioScriptPath, request.getUuid(), request.getLeadId());

            Map<String, Object> scoringResultsMap = (Map<String, Object>) llmExtractionResult.get("scoring_results");
            Double overallScore = (Double) scoringResultsMap.get("overall_score");

            Map<String, Object> fieldByFieldScoresMap = (Map<String, Object>) scoringResultsMap.get("field_by_field_scores");
            Map<String, Double> fieldByFieldScores = new HashMap<>();
            if (fieldByFieldScoresMap != null) {
                for (Map.Entry<String, Object> entry : fieldByFieldScoresMap.entrySet()) {
                    fieldByFieldScores.put(entry.getKey(), ((Number) entry.getValue()).doubleValue()); // Cast to Number and then to double
                }
            }

            Map<String, Object> explanationMap = (Map<String, Object>) scoringResultsMap.get("explanation");
            List<String> explanation = new ArrayList<>();
            if (explanationMap != null) {
                for (Map.Entry<String, Object> entry : explanationMap.entrySet()) {
                    explanation.add(entry.getValue().toString());
                }
            }

            String status = (String) llmExtractionResult.get("status");

            logger.info("Extracted Data: {}", Arrays.asList(
                    "ReferenceName=" + referenceName,
                    "SubjectName=" + subjectName,
                    "SubjectAddress=" + subjectAddress,
                    "RelationToSubject=" + relationToSubject,
                    "SubjectOccupation=" + subjectOccupation,
                    "OverallScore=" + overallScore,
                    "FieldByFieldScores=" + fieldByFieldScores,
                    "Explanation=" + explanation,
                    "Status=" + status,
                    "AudioAnalysis="+audioAnalysisMap
            ));
            // Convert transcriptList to a list of strings
            List<String> transcript = transcriptList.stream().map(map -> map.toString()).collect(Collectors.toList());
            // Create and return AudioResponse
          //  return new AudioResponse(request.getUuid(), request.getAgentId(),llmExtractionResult, audioAnalysisMap, transcript, referenceName, subjectName, subjectAddress, relationToSubject, subjectOccupation, overallScore, explanation, fieldByFieldScores, status);
            AudioResponse audioResponse = new AudioResponse(request.getUuid(), request.getAgentId(),llmExtractionResult, audioAnalysisMap, transcript, referenceName, subjectName, subjectAddress, relationToSubject, subjectOccupation, overallScore, explanation, fieldByFieldScores, status);
            System.out.println("AudioResponse: " + audioResponse);  // Print the object's contents
            return audioResponse;
        }
        catch(Exception e){
            logger.error("Exception during procesing",e);
            return null;
        }
    }

    private Map<String, Object> processAudioScript(String scriptPath, String uuid, String leadId) {
        logger.debug("Running Python script: {} with audio file: {} and lead id: {}", scriptPath, uuid, leadId);
        Map<String, Object> result = pythonExecutor.runPythonScript(scriptPath, uuid, leadId);

        if (result == null || result.isEmpty()) {
            logger.warn("Python script {} returned an empty result for audio file: {} for lead Id: {}", scriptPath, uuid, leadId);
        } else {
            logger.info("Python script {} execution successful for lead Id: {}", scriptPath, leadId);
        }

        return result;
    }
}