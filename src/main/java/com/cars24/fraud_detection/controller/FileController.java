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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("fileType") String fileType) {

        try {
            byte[] fileData = file.getBytes();
            FileEntity savedFile = fileService.uploadFile(agentId, leadId, fileType, file.getOriginalFilename(), fileData);

            return new ResponseEntity<>(savedFile, HttpStatus.CREATED);

        } catch (IOException e) {
            log.error("Error during file upload: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileResponse>> uploadMultipleFiles(
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("fileTypes") List<String> fileTypes,
            @RequestParam("files") List<MultipartFile> files) {

        List<FileResponse> uploadedFiles = fileService.uploadMultipleFiles(agentId, leadId, fileTypes, files);
        return ResponseEntity.ok(uploadedFiles);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileEntity> getFile(@PathVariable String fileId) {
        FileEntity fileEntity = fileService.getFile(fileId);
        return new ResponseEntity<>(fileEntity, HttpStatus.OK);
    }

    @GetMapping("/{agentId}/{leadId}/{fileType}")
    public List<FileEntity> getFiles(
            @PathVariable String agentId,
            @PathVariable String leadId,
            @PathVariable String fileType) {
        return fileService.getFilesByAgentAndLead(agentId, leadId, fileType);
    }

    @GetMapping("/{agentId}/{leadId}/active")
    public List<FileEntity> getActiveFilesByAgentAndLead(
            @PathVariable String agentId,
            @PathVariable String leadId) {
        return fileService.getActiveFilesByAgentAndLead(agentId, leadId);
    }

    @GetMapping("/{agentId}/{leadId}")
    public List<FileEntity> getFilesByAgentAndLead(
            @PathVariable String agentId,
            @PathVariable String leadId) {
        return fileService.getFilesByAgentAndLead(agentId, leadId);
    }


}