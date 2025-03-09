package com.cars24.fraud_detection.workflow.impl;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
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
public class WorkflowInitiatorImpl implements WorkflowInitiator {

    private final PythonExecutor pythonExecutor;
    private static final String STORAGE_PATH = "src/main/resources/document_storage/";
    private final ExecutorService executor; // Injected from Spring configuration

    @Override
    public DocumentResponse processDocument(DocumentRequest request) {
        String documentPath = saveDocument(request.getDocumentData(), request.getFileName());
        log.info("Document stored at: {}", documentPath);

        try {
            // Run OCR Extraction
            String ocrOutput = pythonExecutor.runPythonScript("DocumentOcr.py", documentPath);
            log.info("Raw OCR output: {}", ocrOutput);

            // Parse JSON with better error handling
            JSONObject ocrResult;
            try {
                ocrResult = new JSONObject(ocrOutput);

                if (ocrResult.has("error")) {
                    String errorMessage = ocrResult.getString("error");
                    log.error("OCR script returned an error: {}", errorMessage);
                    throw new DocumentProcessingException("OCR processing error: " + errorMessage);
                }

            } catch (JSONException e) {
                log.error("Failed to parse OCR output as JSON: {}", e.getMessage());
                log.error("Raw OCR output: '{}'", ocrOutput);
                throw new DocumentProcessingException("Failed to parse OCR output: " + e.getMessage());
            }

            if (!ocrResult.has("ocr_json_path")) {
                log.error("OCR extraction failed, missing ocr_json_path.");
                throw new DocumentProcessingException("OCR extraction failed, validation cannot proceed.");
            }

            String ocrJsonPath = ocrResult.getString("ocr_json_path");
            log.info("OCR JSON stored at: {}", ocrJsonPath);

            // Check if the OCR JSON file exists
            File ocrJsonFile = new File(ocrJsonPath);
            if (!ocrJsonFile.exists()) {
                log.error("OCR JSON file does not exist at path: {}", ocrJsonPath);
                throw new DocumentProcessingException("OCR JSON file not found at: " + ocrJsonPath);
            }

            // Submit parallel tasks for quality and forgery analysis
            Future<Object> qualityFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentQuality.py", documentPath)
            );

            Future<Object> forgeryFuture = executor.submit(() ->
                    pythonExecutor.runPythonScript("DocumentForgery.py", documentPath)
            );

            // Check forgery result before proceeding with validation.  This is the short circuit we talked about
            JSONObject forgeryResult;
            try {
                forgeryResult = parseResult(forgeryFuture.get(), "forgery");
                // Check if forgery detection detected a forgery
                if (isForgeryDetected(forgeryResult)) {
                    throw new DocumentProcessingException("Document processing stopped due to forgery detection.");
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error during forgery detection: {}", e.getMessage(), e);
                throw new DocumentProcessingException("Error during forgery detection: " + e.getMessage());
            }

            Future<Object> validationFuture = executor.submit(() -> {
                log.info("Running DocumentValidation with OCR JSON path: {}", ocrJsonPath);
                return pythonExecutor.runPythonScript("DocumentValidation.py", ocrJsonPath);
            });

            // Process results with better error handling
            JSONObject qualityResult = parseResult(qualityFuture.get(), "quality");

            JSONObject validationResult;
            try {
                validationResult = parseResult(validationFuture.get(), "validation");

                if (validationResult.has("validation_results")) {
                    JSONObject validationDetails = validationResult.getJSONObject("validation_results");
                    //Add logic here to throw an exception if you can't extract the values

                }

            } catch (InterruptedException | ExecutionException e) {
                log.error("Error during validation: {}", e.getMessage(), e);
                throw new DocumentProcessingException("Error during document validation: " + e.getMessage());
            }

            double fraudRiskScore = computeRiskScore(ocrResult, qualityResult, forgeryResult, validationResult);
            Map<String, Object> aggregatedResults = aggregateResults(ocrResult, qualityResult, forgeryResult, validationResult);

            log.info("Final Aggregated Results: {}", aggregatedResults);

            return new DocumentResponse(
                    null,
                    true,
                    aggregatedResults,
                    fraudRiskScore,
                    extractIndividualScores(qualityResult, forgeryResult, validationResult),
                    "Processing Successful"
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
            log.error("Execution error while processing document: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new DocumentProcessingException("Document processing execution error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Document processing failed: " + e.getMessage());
        }
    }

    private boolean isForgeryDetected(JSONObject forgeryResult) {
        if (forgeryResult.has("Detailed Insights")) {
            JSONObject detailedInsights = forgeryResult.getJSONObject("Detailed Insights");
            if (detailedInsights.has("Overall Assessment")) {
                JSONObject overall = detailedInsights.getJSONObject("Overall Assessment");
                if (overall.has("conclusion")) {
                    String conclusion = overall.getString("conclusion");
                    return conclusion.toLowerCase().contains("forgery");
                }
            }
        }
        return false;
    }

    private JSONObject parseResult(Object result, String stage) {
        try {
            if (result == null) {
                log.warn("{} result is null", stage);
                return new JSONObject("{}");
            }

            String resultStr = result.toString().trim();
            log.debug("{} result string: {}", stage, resultStr);

            return new JSONObject(resultStr);
        } catch (JSONException e) {
            log.error("Failed to parse {} result as JSON: {}", stage, e.getMessage());
            log.error("Raw {} result: '{}'", stage, result);

            // Return an empty JSON object rather than failing
            return new JSONObject("{}");
        }
    }

    private String saveDocument(byte[] fileData, String fileName) {
        try {
            Files.createDirectories(Paths.get(STORAGE_PATH));
            File file = new File(STORAGE_PATH + fileName);
            Files.write(file.toPath(), fileData);
            return file.getAbsolutePath();
        } catch (Exception e) {
            log.error("Error storing document: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to store document.");
        }
    }

    private Map<String, Object> aggregateResults(JSONObject ocr, JSONObject quality, JSONObject forgery, JSONObject validation) {
        Map<String, Object> results = new HashMap<>();
        results.put("ocrResults", safeToMap(ocr));
        results.put("qualityResults", safeToMap(quality));
        results.put("forgeryResults", safeToMap(forgery));
        results.put("validationResults", safeToMap(validation));
        return results;
    }

    private Map<String, Object> safeToMap(JSONObject json) {
        try {
            return json.toMap();
        } catch (Exception e) {
            log.warn("Failed to convert JSONObject to Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private double computeRiskScore(JSONObject ocr, JSONObject quality, JSONObject forgery, JSONObject validation) {
        //Weights for each module
        double ocrWeight = 0.1; // 10%
        double qualityWeight = 0.2; // 20%
        double forgeryWeight = 0.4; // 40%
        double validationWeight = 0.3; // 30%

        //Extract scores from each JSON object safely with default values
        double ocrScore = extractOcrScore(ocr);
        double qualityScore = extractQualityScore(quality);
        double forgeryScore = extractForgeryScore(forgery);
        double validationScore = extractValidationScore(validation);

        //Apply weights to each score and sum them up
        return (ocrScore * ocrWeight) + (qualityScore * qualityWeight) + (forgeryScore * forgeryWeight) + (validationScore * validationWeight);
    }

    //Extract the OCR Score
    private double extractOcrScore(JSONObject ocr){
        //Base score for OCR is 0.  We are not penalizing document just because the document has poor quality
        double ocrScore = 0.0;
        String text = ocr.optString("text", "");

        //If OCR text is not empty then increase the score to 1
        if (!text.isEmpty()){
            ocrScore = 1.0; //Give 1.0 score if any text has been extracted
        }

        return ocrScore;
    }

    //Helper function to safely extract Quality Score, with a default score of 0 if missing
    private double extractQualityScore(JSONObject quality){
        return parseDouble(quality.opt("overall_quality_score"));
    }

    //Helper function to safely extract Forgery Score with a default score of 0 if missing
    private double extractForgeryScore(JSONObject forgery) {
        double tamperingScore = 0.0;
        if (forgery.has("Detailed Insights")) {
            JSONObject detailedInsights = forgery.getJSONObject("Detailed Insights");
            if (detailedInsights.has("Overall Assessment")) {
                JSONObject overall = detailedInsights.getJSONObject("Overall Assessment");
                if (overall.has("confidence")) {
                    tamperingScore = parseDouble(overall.opt("confidence"));
                }
            }
        }

        return tamperingScore;
    }

    //Helper function to safely extract Validation Score with a default score of 0 if missing
    private double extractValidationScore(JSONObject validation) {
        double validationScore = 0.0;
        if (validation.has("validation_results")) {
            JSONObject validationResults = validation.getJSONObject("validation_results");
            validationScore = parseDouble(validationResults.opt("overall_validation_score"));
        }
        return validationScore;
    }

    private Map<String, Double> extractIndividualScores(JSONObject quality, JSONObject forgery, JSONObject validation) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("qualityScore", parseDouble(quality.opt("overall_quality_score")));
        scores.put("forgeryScore", parseDouble(forgery.opt("Tampering Detection Score")));

        double validationScore = 0;
        if (validation.has("validation_results")) {
            validationScore = parseDouble(validation.getJSONObject("validation_results").opt("overall_validation_score"));
        }
        scores.put("validationScore", validationScore);
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