package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.cars24.fraud_detection.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DocumentWorkflow implements WorkflowInitiator {

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflow.class);

    private final PythonExecutor pythonExecutor;
    private final FileUtils fileUtils;

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

    private static final int FUTURE_TIMEOUT_SECONDS = 30;
    private static final int EXECUTOR_TERMINATION_TIMEOUT_SECONDS = 5;

    public DocumentWorkflow(PythonExecutor pythonExecutor, FileUtils fileUtils) {
        this.pythonExecutor = pythonExecutor;
        this.fileUtils = fileUtils;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        fileUtils.validateRequest(request);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        String documentPath = null;
        DocumentResponse response = null;

        // Retrieve userReportId from the request.  Handle potential null/empty case.
        String userReportId = request.getUserReportId(); // Assuming this is the correct getter
        if (userReportId == null || userReportId.isEmpty()) {
            log.warn("userReportId missing in DocumentRequest. Generating a new one.");
            //userReportId = UUID.randomUUID().toString(); // Generate if missing
        }
        log.info("Using userReportId: {}", userReportId);

        // Generate a separate documentId
        String documentId = UUID.randomUUID().toString();
        log.info("Generated documentId: {}", documentId);

        try {
            // Create necessary directories
            Files.createDirectories(Paths.get(archivePath));

            // Save the document and get its path, using the userReportId
            documentPath = fileUtils.saveDocument(request.getDocumentData(), request.getFileName());
            log.info("Document stored at: {}", documentPath);

            // Execute OCR
            Map<String, Object> ocrResult = fileUtils.executeOcr(executor, documentPath);
            log.info("OCR Result: {}", ocrResult);

            if (ocrResult == null || ocrResult.isEmpty() || !ocrResult.containsKey("ocr_json_path")) {
                throw new DocumentProcessingException("OCR JSON path not found in OCR result.");
            }
            String ocrJsonPath = fileUtils.extractOcrJsonPath(ocrResult);

            // Execute independent tasks in parallel
            Map<String, Object> qualityResult = fileUtils.executePythonTask(executor, fileUtils.getQualityScriptPath(), documentPath, "Quality Analysis");
            Map<String, Object> forgeryResult = fileUtils.executePythonTask(executor, fileUtils.getForgeryScriptPath(), documentPath, "Forgery Detection");
            Map<String, Object> validationResult = fileUtils.executePythonTask(executor, fileUtils.getValidationScriptPath(), ocrJsonPath, "Validation");

            // Compute fraud risk score
            double fraudRiskScore = fileUtils.computeRiskScore(qualityResult, forgeryResult, validationResult);

            // Create response with appropriate documentId, userReportId, and documentPath
            response = fileUtils.createResponse(userReportId ,documentId,fraudRiskScore, ocrResult, qualityResult, forgeryResult, validationResult);

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        } finally {
            fileUtils.shutdownExecutor(executor);
            fileUtils.cleanupFile(documentPath);
        }

        return response;
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        return null;
    }
}