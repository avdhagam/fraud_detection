package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class DocumentWorkflow implements WorkflowInitiator {
    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflow.class);

    private final PythonExecutor pythonExecutor;
    private static final String STORAGE_PATH = "src/main/resources/document_storage/";

    public DocumentWorkflow(PythonExecutor pythonExecutor) {
        this.pythonExecutor = pythonExecutor;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            // ✅ Step 1: Store the document
            final String documentPath = saveDocument(request.getDocumentData(), request.getFileName());
            log.info("Document stored at: {}", documentPath);

            // ✅ Step 2: Run OCR first (so validation gets correct data)
            Future<Map<String, Object>> ocrFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("src/main/resources/python_workflows/DocumentOcr.py", documentPath)
            );

            Map<String, Object> ocrResult;
            try {
                ocrResult = ocrFuture.get(10, TimeUnit.SECONDS); // Timeout after 10s
                log.info("OCR Extraction Completed: {}", ocrResult);
            } catch (TimeoutException e) {
                log.error("OCR processing timeout!", e);
                throw new DocumentProcessingException("OCR processing timed out.");
            }

            // ✅ Step 3: Run quality, forgery, and validation in parallel
            Future<Map<String, Object>> qualityFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("src/main/resources/python_workflows/DocumentQuality.py", documentPath)
            );

            Future<Map<String, Object>> forgeryFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("src/main/resources/python_workflows/DocumentForgery.py", documentPath)
            );

            Future<Map<String, Object>> validationFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("src/main/resources/python_workflows/DocumentValidation.py", documentPath, ocrResult)
            );

            // ✅ Step 4: Collect results with timeouts to prevent blocking
            Map<String, Object> qualityResult = getFutureResult(qualityFuture, "Quality Analysis");
            Map<String, Object> forgeryResult = getFutureResult(forgeryFuture, "Forgery Detection");
            Map<String, Object> validationResult = getFutureResult(validationFuture, "Validation");

            // ✅ Step 5: Compute risk score
            double fraudRiskScore = computeRiskScore(qualityResult, forgeryResult, validationResult);
            Map<String, Object> aggregatedResults = aggregateResults(ocrResult, qualityResult, forgeryResult, validationResult);

            log.info("Final Aggregated Results: {}", aggregatedResults);

            return new DocumentResponse(
                    UUID.randomUUID().toString(), // Generate a unique Document ID
                    true,
                    aggregatedResults,
                    fraudRiskScore,
                    extractIndividualScores(qualityResult, forgeryResult, validationResult),
                    "Processing Successful"
            );

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        } finally {
            executor.shutdown(); // ✅ Ensure executor is properly shut down
        }
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) {
        return null;
    }

    /**
     * ✅ Helper method to safely get future results with timeout.
     */
    private Map<String, Object> getFutureResult(Future<Map<String, Object>> future, String processName) {
        try {
            return future.get(10, TimeUnit.SECONDS); // Timeout after 10s
        } catch (TimeoutException e) {
            log.error("{} processing timeout!", processName, e);
            return Map.of("error", processName + " timed out"); // Return default response
        } catch (Exception e) {
            log.error("Error during {} processing: {}", processName, e.getMessage(), e);
            return Map.of("error", processName + " failed");
        }
    }

    /**
     * Stores the document and returns its file path.
     */
    private String saveDocument(byte[] fileData, String fileName) {
        try {
            Files.createDirectories(Paths.get(STORAGE_PATH)); // ✅ Ensure directory exists

            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            File file = new File(STORAGE_PATH + uniqueFileName);
            Files.write(file.toPath(), fileData);

            return file.getAbsolutePath();
        } catch (Exception e) {
            log.error("Error storing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to store document.");
        }
    }

    /**
     * Aggregates all extracted results into a single map.
     */
    private Map<String, Object> aggregateResults(Map<String, Object> ocr, Map<String, Object> quality,
                                                 Map<String, Object> forgery, Map<String, Object> validation) {
        return Map.of(
                "ocrResults", ocr,
                "qualityResults", quality,
                "forgeryResults", forgery,
                "validationResults", validation
        );
    }

    /**
     * Computes fraud risk score based on different metrics.
     */
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

    /**
     * Extracts individual risk scores from different validation checks.
     */
    private Map<String, Double> extractIndividualScores(Map<String, Object> quality, Map<String, Object> forgery, Map<String, Object> validation) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("qualityScore", parseDouble(quality.get("score")));
        scores.put("forgeryScore", parseDouble(forgery.get("risk")));
        scores.put("validationScore", parseDouble(validation.get("confidence")));
        return scores;
    }

    /**
     * Safely converts an object to a double.
     */
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