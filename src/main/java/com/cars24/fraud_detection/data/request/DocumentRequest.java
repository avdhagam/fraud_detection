package com.cars24.fraud_detection.data.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentRequest {

    private String agentId;
    private String leadId;

    private String documentType;

    private String fileName;
    private byte[] documentData;  // Directly store the byte data
}