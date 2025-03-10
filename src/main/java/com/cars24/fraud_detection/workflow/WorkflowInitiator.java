package com.cars24.fraud_detection.workflow;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface WorkflowInitiator {
    DocumentResponse processDocument(DocumentRequest request);

    AudioResponse processAudio(AudioRequest request) throws JsonProcessingException, AudioProcessingException;
}
