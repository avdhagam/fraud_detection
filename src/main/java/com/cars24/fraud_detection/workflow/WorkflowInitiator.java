package com.cars24.fraud_detection.workflow;

public interface WorkflowInitiator {
    Map<String, Object> startWorkflow(MultipartFile file);
}

