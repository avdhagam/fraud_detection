package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import com.cars24.fraud_detection.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentTypeConfig documentTypeConfig;

    public DocumentController(DocumentService documentService, DocumentTypeConfig documentTypeConfig) {
        this.documentService = documentService;
        this.documentTypeConfig = documentTypeConfig;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("documentType") String documentType) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        DocumentRequest request = new DocumentRequest();
        request.setFileName(file.getOriginalFilename());
        request.setDocumentData(file.getBytes());
        request.setAgentId(agentId);
        request.setLeadId(leadId);
        request.setDocumentType(documentType);

        DocumentResponse response = documentService.processDocument(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String documentId) {
        DocumentResponse response = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/image/{documentId}")
    public ResponseEntity<Resource> getDocumentImage(@PathVariable String documentId) {
        try {
            DocumentResponse document = documentService.getDocumentById(documentId);

            if (document == null || document.getDocumentId() == null) {
                log.warn("Document not found or file path is null for document ID: {}", documentId);
                return ResponseEntity.notFound().build();
            }

            String filePath = documentService.findDocumentEntityById(documentId).getFilePath();

            Path imagePath = Paths.get(filePath);

            if (!Files.exists(imagePath)) {
                log.warn("Image file not found at path: {}", imagePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            Resource imageResource = new ByteArrayResource(Files.readAllBytes(imagePath));

            return ResponseEntity.ok()
                    .contentLength(Files.size(imagePath))
                    .contentType(MediaType.IMAGE_JPEG) // Adjust MediaType based on your image type (e.g., IMAGE_PNG)
                    .body(imageResource);

        } catch (IOException e) {
            log.error("Error reading image file for document ID: {}", documentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent/{leadId}")
    public ResponseEntity<List<String>> getRecentDocumentNames(
            @PathVariable String leadId,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Fetching last {} documents for lead ID: {}", limit, leadId);

        List<String> fileNames = documentService.getRecentDocumentNames(leadId, limit);

        return ResponseEntity.ok(fileNames);
    }

//    @GetMapping("/{leadId}/{docType}")
//    public ResponseEntity<DocumentResponse> getDocumentByLeadIdAndType(
//            @PathVariable String leadId,
//            @PathVariable String docType) {
//        DocumentResponse document = documentService.getDocumentByLeadIdAndType(leadId, docType)
//                .map(DocumentEntity::toResponse)
//                .orElse(null); // Or handle not found case as appropriate
//        return ResponseEntity.ok(document);
//    }

    @GetMapping("/types")
    public ResponseEntity<Map<String, String>> getAllDocumentTypes() {
        System.out.println("Fetching document types: " + documentTypeConfig.getMapping());
        return ResponseEntity.ok(documentTypeConfig.getMapping());
    }

    @GetMapping("/{leadId}/{docType}")
    public ResponseEntity<DocumentResponse> getDocumentsByLeadIdAndType(
            @PathVariable String leadId,
            @PathVariable String docType) {
        DocumentResponse document = documentService.getDocumentByLeadIdAndType(leadId, docType)
                .map(DocumentEntity::toResponse)
                .orElse(null); // Handle not found case

        return ResponseEntity.ok(document);
    }
}