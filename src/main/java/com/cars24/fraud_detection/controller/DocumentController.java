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
import java.util.Optional;
import java.util.stream.Collectors;

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

    @PutMapping("/update-document")
    public ResponseEntity<DocumentResponse> updateDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("documentType") String documentType) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        DocumentRequest request = new DocumentRequest();
        request.setFileName(file.getOriginalFilename());
        request.setDocumentData(file.getBytes());
        request.setUserReportId(userId);
        request.setDocumentType(documentType);

        DocumentResponse response = documentService.processDocument(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/upload-document")
    public ResponseEntity<?> uploadDocument(
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "userReportId", required = true) String userReportId) {

        try {
            //  Debugging: Print received parameters
            System.out.println("Received request to upload document...");
            System.out.println("File Received: " + (file != null ? file.getOriginalFilename() : "NO FILE"));
            System.out.println("User Report ID: " + userReportId);

            // Check if file is actually received
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: No file uploaded!");
            }

            //  Create document request object
            DocumentRequest request = new DocumentRequest();
            request.setFileName(file.getOriginalFilename());
            request.setDocumentData(file.getBytes());
            request.setUserReportId(userReportId); // Ensure `DocumentRequest` has this field

            // Process the document
            DocumentResponse response = documentService.processDocument(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing document: " + e.getMessage());
        }
    }

    @GetMapping("/image/{documentId}")
    public ResponseEntity<Resource> getDocumentImage(@PathVariable String documentId) {
        try {
            DocumentEntity document = documentService.findDocumentEntityById(documentId);

            if (document == null || document.getFilePath() == null) {
                log.warn("Document not found or file path is null for document ID: {}", documentId);
                return ResponseEntity.notFound().build();
            }

            String filePath = document.getFilePath();
            log.info("File path retrieved from database: {}", filePath); //DEBUG

            Path imagePath = Paths.get(filePath);

            log.info("Image path: {}", imagePath.toAbsolutePath()); //DEBUG

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

    @GetMapping("/types")
    public ResponseEntity<Map<String, String>> getAllDocumentTypes() {
        System.out.println("Fetching document types: " + documentTypeConfig.getMapping());
        return ResponseEntity.ok(documentTypeConfig.getMapping());
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<String>> getRecentDocumentNames(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Fetching last {} documents for user ID: {}", limit, userId);

        List<String> fileNames = documentService.getRecentDocuments(userId, limit);

        return ResponseEntity.ok(fileNames);
    }

    @GetMapping("/{userId}/{docType}")
    public Optional<DocumentEntity> getDocument(
            @PathVariable String userId,
            @PathVariable String docType) {
        return documentService.getDocumentByUserIdAndType(userId, docType);
    }

}
