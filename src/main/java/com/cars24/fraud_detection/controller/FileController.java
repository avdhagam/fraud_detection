package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.FileEntity;
import com.cars24.fraud_detection.data.response.FileResponse;
import com.cars24.fraud_detection.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS})
public class FileController {

    private final FileService fileService;

    // Secure mapping storage (Consider using DB or Redis in production)
    private final Map<String, String> secureIdMapping = new ConcurrentHashMap<>();

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("fileType") String fileType) {

        // Validate UUIDs
        if (!isValidUUID(agentId) || !isValidUUID(leadId)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] fileData = file.getBytes();

            // Get or generate secure IDs
            String safeAgentId = getOrGenerateSecureId(agentId);
            String safeLeadId = getOrGenerateSecureId(leadId);

            FileEntity savedFile = fileService.uploadFile(safeAgentId, safeLeadId, fileType, file.getOriginalFilename(), fileData);

            FileResponse fileResponse = new FileResponse(
                    savedFile.getFileId(),
                    savedFile.getAgentId(),
                    savedFile.getLeadId(),
                    savedFile.getOriginalFilename(),
                    savedFile.getFileType(),
                    savedFile.getFilePath(),
                    savedFile.getStatus(),
                    savedFile.getIsActive(),
                    savedFile.getUploadedAt()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(fileResponse);

        } catch (IOException e) {
            log.error("Error during file upload: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileResponse>> uploadMultipleFiles(
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("fileTypes") List<String> fileTypes,
            @RequestParam("files") List<MultipartFile> files) {

        // Validate UUIDs
        if (!isValidUUID(agentId) || !isValidUUID(leadId)) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Use securely mapped IDs
        String safeAgentId = getOrGenerateSecureId(agentId);
        String safeLeadId = getOrGenerateSecureId(leadId);

        List<FileResponse> uploadedFiles = fileService.uploadMultipleFiles(safeAgentId, safeLeadId, fileTypes, files);
        return ResponseEntity.ok(uploadedFiles);
    }


    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getFile(@PathVariable String fileId) {
        FileEntity fileEntity = fileService.getFile(fileId);
        if (fileEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        FileResponse fileResponse = new FileResponse(
                fileEntity.getFileId(),
                fileEntity.getAgentId(),
                fileEntity.getLeadId(),
                fileEntity.getOriginalFilename(),
                fileEntity.getFileType(),
                fileEntity.getFilePath(),
                fileEntity.getStatus(),
                fileEntity.getIsActive(),
                fileEntity.getUploadedAt()
        );
        return ResponseEntity.ok(fileResponse);
    }

    @GetMapping("/{agentId}/{leadId}/{fileType}")
    public ResponseEntity<List<FileEntity>> getFiles(
            @PathVariable String agentId,
            @PathVariable String leadId,
            @PathVariable String fileType) {
        if (!isValidUUID(agentId) || !isValidUUID(leadId)) {
            return ResponseEntity.badRequest().build();
        }
        List<FileEntity> files = fileService.getFilesByAgentAndLead(agentId, leadId, fileType);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{agentId}/{leadId}/active")
    public ResponseEntity<List<FileEntity>> getActiveFilesByAgentAndLead(
            @PathVariable String agentId,
            @PathVariable String leadId) {
        if (!isValidUUID(agentId) || !isValidUUID(leadId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(fileService.getActiveFilesByAgentAndLead(agentId, leadId));
    }

    @GetMapping("/{agentId}/{leadId}")
    public ResponseEntity<List<FileEntity>> getFilesByAgentAndLead(
            @PathVariable String agentId,
            @PathVariable String leadId) {
        if (!isValidUUID(agentId) || !isValidUUID(leadId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(fileService.getFilesByAgentAndLead(agentId, leadId));
    }

    private boolean isValidUUID(String input) {
        return input.matches("^[a-fA-F0-9\\-]{36}$");
    }

    private String getOrGenerateSecureId(String originalId) {
        return secureIdMapping.computeIfAbsent(originalId, k -> UUID.randomUUID().toString());
    }
}
