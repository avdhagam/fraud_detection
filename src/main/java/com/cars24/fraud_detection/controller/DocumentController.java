package com.cars24.fraud_detection.controller;

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

