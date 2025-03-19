package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.service.DocumentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentTypeConfig documentTypeConfig;

    public DocumentController(DocumentService documentService, DocumentTypeConfig documentTypeConfig) {
        this.documentService = documentService;
        this.documentTypeConfig = documentTypeConfig;
    }

    @PostMapping("/process")
    public ResponseEntity<DocumentResponse> processDocument(@RequestParam("file") MultipartFile file) throws IOException {
        DocumentRequest request = new DocumentRequest();
        request.setFileName(file.getOriginalFilename());
        request.setDocumentData(file.getBytes());

        DocumentResponse response = documentService.processDocument(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/result")
    public ResponseEntity<DocumentResponse> getDocument(@RequestParam("documentId") String documentId) {
        DocumentResponse response = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    public ResponseEntity<Map<String, String>> getAllDocumentTypes() {
        System.out.println("Fetching document types: " + documentTypeConfig.getMapping());
        return ResponseEntity.ok(documentTypeConfig.getMapping());
    }


    @GetMapping("/types/{key}")
    public ResponseEntity<String> getDocumentTypeByKey(@PathVariable String key) {
        return ResponseEntity.ok(documentTypeConfig.getDocumentType(key));
    }
}
