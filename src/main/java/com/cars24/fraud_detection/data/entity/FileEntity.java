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

    private String agentId;
    private String leadId;
    private String originalFilename;
    private String fileType;
    private String filePath;
    private String status;
    private Boolean isActive;
    private LocalDateTime uploadedAt;

    public FileEntity(String agentId, String leadId, String originalFilename, String fileType, String filePath) {
        this.fileId = UUID.randomUUID().toString();
        this.agentId = agentId;
        this.leadId = leadId;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.filePath = filePath;
        this.status = "PENDING";
        this.isActive = Boolean.TRUE;
        this.uploadedAt = LocalDateTime.now();
    }
}
