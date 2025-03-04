package com.cars24.fraud_detection.workflow.impl;

@Service
public class DocumentWorkflow implements WorkflowInitiator {

    @Autowired
    private PythonExecutor pythonExecutor;

    @Override
    public Map<String, Object> startWorkflow(MultipartFile file) {
        Map<String, Object> results = new HashMap<>();

        // Convert file to temp path
        String filePath = saveTempFile(file);

        // Execute Python scripts (Parallel Processing)
        CompletableFuture<String> ocrFuture = CompletableFuture.supplyAsync(() -> pythonExecutor.runScript("ocr.py", filePath));
        CompletableFuture<String> validationFuture = CompletableFuture.supplyAsync(() -> pythonExecutor.runScript("validation.py", filePath));
        CompletableFuture<String> imageQualityFuture = CompletableFuture.supplyAsync(() -> pythonExecutor.runScript("image_quality.py", filePath));
        CompletableFuture<String> forgeryFuture = CompletableFuture.supplyAsync(() -> pythonExecutor.runScript("forgery.py", filePath));

        CompletableFuture.allOf(ocrFuture, validationFuture, imageQualityFuture, forgeryFuture).join();

        // Get Results
        results.put("ocr", ocrFuture.join());
        results.put("validation_score", Double.parseDouble(validationFuture.join()));
        results.put("image_quality_score", Double.parseDouble(imageQualityFuture.join()));
        results.put("forgery_score", Double.parseDouble(forgeryFuture.join()));

        // Run Risk Aggregator
        String finalRisk = pythonExecutor.runScript("risk_aggregator.py", results);
        results.put("final_risk", finalRisk);

        return results;
    }

    private String saveTempFile(MultipartFile file) {
        // Save uploaded file temporarily and return the path
    }
}

