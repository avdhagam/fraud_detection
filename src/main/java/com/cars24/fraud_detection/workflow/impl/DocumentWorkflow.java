package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor3;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class DocumentWorkflow implements WorkflowInitiator {

    @Value("${document.storage.path:src/main/resources/document_storage/}")
    private String storagePath;

    @Value("${document.archive.path:src/main/resources/document_storage/archive/}")
    private String archivePath;

    @Value("${python.scripts.ocr.path:src/main/resources/python_workflows/DocumentOcr.py}")
    private String ocrScriptPath;

    @Value("${python.scripts.quality.path:src/main/resources/python_workflows/DocumentQuality.py}")
    private String qualityScriptPath;

    @Value("${python.scripts.forgery.path:src/main/resources/python_workflows/DocumentForgery.py}")
    private String forgeryScriptPath;

    @Value("${python.scripts.validation.path:src/main/resources/python_workflows/DocumentValidation.py}")
    private String validationScriptPath;

    @Value("${risk.scoring.quality.weight:0.3}")
    private double qualityWeight;

    @Value("${risk.scoring.forgery.weight:0.4}")
    private double forgeryWeight;

    @Value("${risk.scoring.validation.weight:0.3}")
    private double validationWeight;

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflow.class);

    private final PythonExecutor3 pythonExecutor;

    private static final int FUTURE_TIMEOUT_SECONDS = 30;
    private static final int MAX_ARCHIVE_RETRIES = 3;

    public DocumentWorkflow(PythonExecutor3 pythonExecutor) {
        this.pythonExecutor = pythonExecutor;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        validateRequest(request);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        String documentPath = null;

        try {
            // Ensure archive directory exists
            Files.createDirectories(Paths.get(archivePath));

            // Store the document
            final String finalDocumentPath = saveDocument(request.getDocumentData(), request.getFileName());
            documentPath = finalDocumentPath; // Store in non-final variable for finally block
            log.info("Document stored at: {}", finalDocumentPath);

            //
            long startOcr = System.currentTimeMillis();
            // Run OCR first (so validation gets correct data)
            Future<Map<String, Object>> ocrFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript(ocrScriptPath, finalDocumentPath)
            );

            Map<String, Object> ocrResult = getFutureResult(ocrFuture, "OCR Extraction");
            long endOcr = System.currentTimeMillis();
            log.info("OCR Extraction Completed in {} ms", (endOcr - startOcr));
            log.info("OCR Extraction Completed: {}", ocrResult);

            // Extract the OCR JSON path from the OCR result
            String ocrJsonPath = (String) ocrResult.get("ocr_json_path"); // Get value of the string
            if (ocrJsonPath == null || ocrJsonPath.isEmpty()) {
                throw new DocumentProcessingException("OCR JSON path not found in OCR result.");
            }

            // Run quality, forgery, and validation in parallel
            // Measure execution time for Quality Analysis
            long startQuality = System.currentTimeMillis();
            Future<Map<String, Object>> qualityFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript(qualityScriptPath, finalDocumentPath)
            );

            // Measure execution time for Forgery Detection
            long startForgery = System.currentTimeMillis();
            Future<Map<String, Object>> forgeryFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript(forgeryScriptPath, finalDocumentPath)
            );

            // Measure execution time for Validation
            long startValidation = System.currentTimeMillis();
            Future<Map<String, Object>> validationFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript(validationScriptPath, ocrJsonPath)
            );

            // Collect results with timeouts to prevent blocking
            Map<String, Object> qualityResult = getFutureResult(qualityFuture, "Quality Analysis");
            long endQuality = System.currentTimeMillis();
            log.info("Quality Analysis Completed in {} ms", (endQuality - startQuality));

            Map<String, Object> forgeryResult = getFutureResult(forgeryFuture, "Forgery Detection");
            long endForgery = System.currentTimeMillis();
            log.info("Forgery Detection Completed in {} ms", (endForgery - startForgery));

            Map<String, Object> validationResult = getFutureResult(validationFuture, "Validation");
            long endValidation = System.currentTimeMillis();
            log.info("Validation Completed in {} ms", (endValidation - startValidation));

            // Compute risk score
            double fraudRiskScore = computeRiskScore(qualityResult, forgeryResult, validationResult);

            // Determine risk level & decision
            String riskLevel = fraudRiskScore > 0.7 ? "HIGH" : fraudRiskScore > 0.4 ? "MEDIUM" : "LOW";
            String decision = fraudRiskScore > 0.7 ? "REJECT" : "APPROVE";
            String nextSteps = fraudRiskScore > 0.7 ? "Request re-submission or manual verification" :
                    fraudRiskScore > 0.4 ? "Manual review recommended" : "No further action needed";

            // Move the processed document to the archive - now properly handles exceptions
            moveToArchive(finalDocumentPath);

            return new DocumentResponse(
                    UUID.randomUUID().toString(),
                    true,
                    fraudRiskScore,
                    riskLevel,
                    decision,
                    nextSteps,
                    "Processing completed successfully",
                    ocrResult,
                    qualityResult,
                    forgeryResult,
                    validationResult
            );

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time, forcing shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Executor shutdown was interrupted", e);
            }

            // Clean up the original file if archiving failed
            if (documentPath != null) {
                try {
                    Path path = Paths.get(documentPath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                        log.info("Cleaned up document file: {}", documentPath);
                    }
                } catch (IOException e) {
                    log.warn("Failed to clean up document file: {}", documentPath, e);
                }
            }
        }
    }

    private void validateRequest(DocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Document request cannot be null");
        }

        if (request.getDocumentData() == null || request.getDocumentData().length == 0) {
            throw new IllegalArgumentException("Document data cannot be empty");
        }

        if (!StringUtils.hasText(request.getFileName())) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
    }

    /**
     * Moves processed documents to the archive folder with retry mechanism.
     *
     * @param documentPath The path of the document to archive
     * @throws DocumentProcessingException if archiving repeatedly fails
     */
    private void moveToArchive(String documentPath) throws DocumentProcessingException {
        Path source = Paths.get(documentPath);
        Path destination = Paths.get(archivePath, source.getFileName().toString());

        Exception lastException = null;
        for (int attempt = 0; attempt < MAX_ARCHIVE_RETRIES; attempt++) {
            try {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                log.info("Document moved to archive: {}", destination);

                // Delete temporary file after successful move
                Files.deleteIfExists(source);
                return;
            } catch (IOException e) {
                lastException = e;
                log.warn("Archive attempt {} failed: {}", attempt + 1, e.getMessage());
                try {
                    Thread.sleep(100 * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DocumentProcessingException("Archive operation interrupted", ie);
                }
            }
        }

        log.error("Failed to move document to archive after {} attempts", MAX_ARCHIVE_RETRIES, lastException);
        throw new DocumentProcessingException("Failed to move document to archive", lastException);
    }


    private Map<String, Object> getFutureResult(Future<Map<String, Object>> future, String processName) {
        try {
            return future.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("{} processing timeout!", processName, e);
            throw new DocumentProcessingException(processName + " processing timed out after " + FUTURE_TIMEOUT_SECONDS + " seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("{} processing interrupted", processName, e);
            throw new DocumentProcessingException(processName + " processing was interrupted");
        } catch (ExecutionException e) {
            log.error("Error during {} processing: {}", processName, e.getMessage(), e);
            throw new DocumentProcessingException(processName + " processing failed: " + e.getCause().getMessage());
        }
    }

    private String saveDocument(byte[] fileData, String fileName) {
        try {
            Files.createDirectories(Paths.get(storagePath)); // Ensure directory exists

            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path filePath = Paths.get(storagePath, uniqueFileName);
            Files.write(filePath, fileData);

            return filePath.toString();
        } catch (Exception e) {
            log.error("Error storing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to store document: " + e.getMessage(), e);
        }
    }

    private double computeRiskScore(Map<String, Object> quality, Map<String, Object> forgery, Map<String, Object> validation) {
        try {
            double qualityScore = parseDouble(quality.get("finalQualityScore"));
            double forgeryScore = parseDouble(forgery.get("finalForgeryRiskScore"));
            double validationScore = parseDouble(validation.get("finalValidationScore"));

            double score = (qualityScore * qualityWeight) + (forgeryScore * forgeryWeight) + (validationScore * validationWeight);
            log.debug("Computed risk score: {} (quality: {}, forgery: {}, validation: {})",
                    score, qualityScore, forgeryScore, validationScore);

            return score;
        } catch (Exception e) {
            log.error("Error computing risk score", e);
            return 0.0;
        }
    }

    private double parseDouble(Object value) {
        try {
            return value instanceof Number ? ((Number) value).doubleValue() :
                    value != null ? Double.parseDouble(value.toString()) : 0.0;
        } catch (NumberFormatException e) {
            log.warn("Invalid number format for value: {}", value);
            return 0.0; // Default to 0 to prevent crashes
        }
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        return null;
    }
}