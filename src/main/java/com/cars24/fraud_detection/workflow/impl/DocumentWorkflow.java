package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentWorkflow implements WorkflowInitiator {

    private final PythonExecutor pythonExecutor;
    private static final String STORAGE_PATH = "stored_documents/";

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            // Step 1: Store the document
            String documentPath = saveDocument(request.getDocumentData(), request.getFileName());
            log.info("Document stored at: {}", documentPath);

            // Step 2: Execute Python scripts in parallel
            Future<Map<String, Object>> ocrFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentOcr.py", documentPath));

            Future<Map<String, Object>> qualityFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentQuality.py", documentPath));

            Future<Map<String, Object>> forgeryFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentForgery.py", documentPath));

            // Step 3: Wait for OCR to complete before running validation
            Map<String, Object> ocrResult = ocrFuture.get();
            log.info("OCR Extraction Completed: {}", ocrResult);

            Future<Map<String, Object>> validationFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentValidation.py", documentPath, ocrResult));

            // Step 4: Collect all results
            Map<String, Object> qualityResult = qualityFuture.get();
            Map<String, Object> forgeryResult = forgeryFuture.get();
            Map<String, Object> validationResult = validationFuture.get();

            // Step 5: Aggregate results and compute risk score
            double fraudRiskScore = computeRiskScore(qualityResult, forgeryResult, validationResult);
            Map<String, Object> aggregatedResults = aggregateResults(ocrResult, qualityResult, forgeryResult, validationResult);

            log.info("Final Aggregated Results: {}", aggregatedResults);

            return new DocumentResponse(
                    null,  // Document ID will be assigned when saved in DB
                    true,
                    aggregatedResults,
                    fraudRiskScore,
                    extractIndividualScores(qualityResult, forgeryResult, validationResult),
                    "Processing Successful"
            );

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        }
    }

    private String saveDocument(byte[] fileData, String fileName) {
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(STORAGE_PATH));

            File file = new File(STORAGE_PATH + fileName);
            Files.write(file.toPath(), fileData);
            return file.getAbsolutePath();
        } catch (Exception e) {
            log.error("Error storing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to store document.");
        }
    }

    private Map<String, Object> aggregateResults(Map<String, Object> ocr, Map<String, Object> quality,
                                                 Map<String, Object> forgery, Map<String, Object> validation) {
        return Map.of(
                "ocrResults", ocr,
                "qualityResults", quality,
                "forgeryResults", forgery,
                "validationResults", validation
        );
    }

    private double computeRiskScore(Map<String, Object> quality, Map<String, Object> forgery, Map<String, Object> validation) {
        try {
            double qualityScore = parseDouble(quality.get("score"));
            double forgeryScore = parseDouble(forgery.get("risk"));
            double validationScore = parseDouble(validation.get("confidence"));

            return (qualityScore * 0.3) + (forgeryScore * 0.4) + (validationScore * 0.3);
        } catch (Exception e) {
            log.error("Error computing risk score: {}", e.getMessage(), e);
            return 0.0; // Default score if calculation fails
        }
    }

    private Map<String, Double> extractIndividualScores(Map<String, Object> quality, Map<String, Object> forgery, Map<String, Object> validation) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("qualityScore", parseDouble(quality.get("score")));
        scores.put("forgeryScore", parseDouble(forgery.get("risk")));
        scores.put("validationScore", parseDouble(validation.get("confidence")));
        return scores;
    }

    private double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                log.warn("Invalid number format for value: {}", value);
            }
        }
        return 0.0;
    }
}
