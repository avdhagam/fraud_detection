package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.response.DocumentResponseDTO;
import com.cars24.fraud_detection.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/process")
    public ResponseEntity<DocumentResponseDTO> processDocument(@RequestParam("file") MultipartFile file) {
        DocumentResponseDTO response = documentService.processDocument(file);
        return ResponseEntity.ok(response);
    }
}

