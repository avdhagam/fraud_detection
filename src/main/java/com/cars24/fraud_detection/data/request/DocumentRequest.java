package com.cars24.fraud_detection.data.request;

import lombok.Data;

@Data
public class DocumentRequest {
    private String userId;
    private String fileName;
    private byte[] documentData;

}
