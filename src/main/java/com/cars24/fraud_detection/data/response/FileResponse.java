package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private String fileId;

    private String agentId;
    private String leadId;
    private String originalFilename;
    private String fileType;
    private String filePath;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Boolean isActive;
    private LocalDateTime uploadedAt;
}