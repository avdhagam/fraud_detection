package com.cars24.fraud_detection.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "files") // MongoDB Collection Name
public class FileEntity {

    @Id
    private String fileId; // Unique ID for the file (String because MongoDB uses ObjectId)

    private String agentId; // ID of the agent who uploaded
    private String leadId; // Lead ID associated with the file
    private String originalFilename; // Original file name
    private String fileType; // AUDIO / DOCUMENT
    private String filePath; // Location where the file is stored
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Boolean isActive; // Soft delete flag
    private LocalDateTime uploadedAt; // Upload timestamp

    public FileEntity(String agentId, String leadId, String originalFilename, String fileType, String filePath) {
        this.fileId = UUID.randomUUID().toString(); // Generate a unique file ID
        this.agentId = agentId;
        this.leadId = leadId;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.filePath = filePath;
        this.status = "PENDING"; // Default status
        this.isActive = Boolean.TRUE; // Mark file as active
        this.uploadedAt = LocalDateTime.now(); // Set timestamp
    }

    public FileEntity() {

    }
}
