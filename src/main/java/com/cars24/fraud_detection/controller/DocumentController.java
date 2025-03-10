package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.service.DocumentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
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
}
