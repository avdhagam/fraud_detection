package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
//@UtilityClass
@Component
public class FileUtils {

    private static final String JPG_EXTENSION = ".jpg";
    private static final String PNG_EXTENSION = ".png";
    private final PythonExecutor3 pythonExecutor = new PythonExecutor3();
    private static final int FUTURE_TIMEOUT_SECONDS = 30;
    private static final int MAX_ARCHIVE_RETRIES = 3;
    private static final long EXECUTOR_TERMINATION_TIMEOUT_SECONDS = 5;

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

    public boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(JPG_EXTENSION) || lowerCaseFileName.endsWith(PNG_EXTENSION);
    }

    public static DocumentEntity buildDocumentEntity(DocumentRequest request, DocumentResponse response) {
        return DocumentEntity.builder()
                .userId(request.getUserId())
                .fileName(request.getFileName())
                .status(response.isValid() ? "COMPLETED" : "FAILED")
                .remarks(response.getRemarks())
                .ocrResults(response.getOcrResults())
                .qualityResults(response.getQualityResults())
                .forgeryResults(response.getForgeryResults())
                .validationResults(response.getValidationResults())
                .finalRiskScore(response.getFinalRiskScore())
                .riskLevel(response.getRiskLevel())
                .decision(response.getDecision())
                .nextSteps(response.getNextSteps())
                .build();
    }


    public Map<String, Object> executeOcr(ExecutorService executor, String documentPath) {
        long startOcr = System.currentTimeMillis();
        Future<Map<String, Object>> ocrFuture = executor.submit(() -> pythonExecutor.runPythonScript(ocrScriptPath, documentPath));
        Map<String, Object> ocrResult = getFutureResult(ocrFuture, "OCR Extraction");
        long endOcr = System.currentTimeMillis();
        log.info("OCR Extraction Completed in {} ms", (endOcr - startOcr));
        log.info("OCR Extraction Completed: {}", ocrResult);
        return ocrResult;
    }

    public String extractOcrJsonPath(Map<String, Object> ocrResult) {
        String ocrJsonPath = (String) ocrResult.get("ocr_json_path");
        if (ocrJsonPath == null || ocrJsonPath.isEmpty()) {
            throw new DocumentProcessingException("OCR JSON path not found in OCR result.");
        }
        return ocrJsonPath;
    }

    public Map<String, Object> executePythonTask(ExecutorService executor, String scriptPath, String parameter, String taskName) {
        long startTime = System.currentTimeMillis();
        Future<Map<String, Object>> future = executor.submit(() -> pythonExecutor.runPythonScript(scriptPath, parameter));
        Map<String, Object> result = getFutureResult(future, taskName);
        long endTime = System.currentTimeMillis();
        log.info("{} Completed in {} ms", taskName, (endTime - startTime));
        return result;
    }

    public DocumentResponse createResponse(double fraudRiskScore, Map<String, Object> ocrResult, Map<String, Object> qualityResult, Map<String, Object> forgeryResult, Map<String, Object> validationResult) {
        String riskLevel = fraudRiskScore > 0.7 ? "HIGH" : fraudRiskScore > 0.4 ? "MEDIUM" : "LOW";
        String decision = fraudRiskScore > 0.7 ? "REJECT" : "APPROVE";
        String nextSteps = fraudRiskScore > 0.7 ? "Request re-submission or manual verification" :
                fraudRiskScore > 0.4 ? "Manual review recommended" : "No further action needed";

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
    }

    public void validateRequest(DocumentRequest request) {
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

    public void moveToArchive(String documentPath) {
        Path source = Paths.get(documentPath);
        Path destination = Paths.get(archivePath, source.getFileName().toString());

        Exception lastException = null;
        for (int attempt = 0; attempt < MAX_ARCHIVE_RETRIES; attempt++) {
            try {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                log.info("Document moved to archive: {}", destination);
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
            Throwable cause = e.getCause() != null ? e.getCause() : e;  // Handle cases with null cause
            throw new DocumentProcessingException(processName + " processing failed: " + cause.getMessage());
        }
    }

    public String saveDocument(byte[] fileData, String fileName) {
        try {
            Files.createDirectories(Paths.get(storagePath));
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path filePath = Paths.get(storagePath, uniqueFileName);
            Files.write(filePath, fileData);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Error storing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to store document: " + e.getMessage(), e);
        }
    }

    public double computeRiskScore(Map<String, Object> quality, Map<String, Object> forgery, Map<String, Object> validation) {
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
            return 0.0;
        }
    }

    public void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(EXECUTOR_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in time, forcing shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Executor shutdown was interrupted", e);
        }
    }

    public void cleanupFile(String documentPath) {
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
    public String getQualityScriptPath() {
        return qualityScriptPath;
    }

    public String getForgeryScriptPath() {
        return forgeryScriptPath;
    }

    public String getValidationScriptPath() {
        return validationScriptPath;
    }


}