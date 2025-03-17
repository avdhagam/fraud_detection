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
import com.cars24.fraud_detection.utils.FileUtils;
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

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflow.class);

    private final PythonExecutor3 pythonExecutor;
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

    public DocumentWorkflow(PythonExecutor3 pythonExecutor, FileUtils fileUtils) {
        this.pythonExecutor = pythonExecutor;
        this.fileUtils = fileUtils;
    }

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        fileUtils.validateRequest(request);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        String documentPath = null;

        try {
            Files.createDirectories(Paths.get(archivePath));
            final String finalDocumentPath = fileUtils.saveDocument(request.getDocumentData(), request.getFileName());
            documentPath = finalDocumentPath;
            log.info("Document stored at: {}", finalDocumentPath);

            Map<String, Object> ocrResult = fileUtils.executeOcr(executor, finalDocumentPath);
            String ocrJsonPath = fileUtils.extractOcrJsonPath(ocrResult);

            //Execute independent tasks in parallel
            Map<String, Object> qualityResult = fileUtils.executePythonTask(executor, fileUtils.getQualityScriptPath(), finalDocumentPath, "Quality Analysis");
            Map<String, Object> forgeryResult = fileUtils.executePythonTask(executor, fileUtils.getForgeryScriptPath(), finalDocumentPath, "Forgery Detection");
            Map<String, Object> validationResult = fileUtils.executePythonTask(executor, fileUtils.getValidationScriptPath(), ocrJsonPath, "Validation");

            double fraudRiskScore = fileUtils.computeRiskScore(qualityResult, forgeryResult, validationResult);
            DocumentResponse response = fileUtils.createResponse(fraudRiskScore, ocrResult, qualityResult, forgeryResult, validationResult);

            fileUtils.moveToArchive(finalDocumentPath);
            return response;

        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        } finally {
            fileUtils.shutdownExecutor(executor);
            fileUtils.cleanupFile(documentPath);
        }
    }

    @Override
    public AudioResponse processAudio(AudioRequest request) throws JsonProcessingException {
        return null;
    }
}