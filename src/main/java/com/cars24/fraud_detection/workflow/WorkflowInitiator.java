package com.cars24.fraud_detection.workflow;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;

public interface WorkflowInitiator {
    DocumentResponse processDocument(DocumentRequest request);
}
