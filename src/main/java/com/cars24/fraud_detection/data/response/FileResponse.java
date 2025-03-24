package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private String fileId; // Unique ID for the file (String because MongoDB uses ObjectId)

    private String agentId; // ID of the agent who uploaded
    private String leadId; // Lead ID associated with the file
    private String originalFilename; // Original file name
    private String fileType; // AUDIO / DOCUMENT
    private String filePath; // Location where the file is stored
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private boolean isActive; // Soft delete flag
    private LocalDateTime uploadedAt; // Upload timestamp
}