package com.cars24.fraud_detection.service;


import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface DocumentService {
    DocumentResponse processDocument(DocumentRequest request);
    DocumentResponse getDocumentById(String documentId);
    DocumentEntity findDocumentEntityById(String documentId);

    List<String> getRecentDocuments(String userId, int limit);
    DocumentEntity getDocumentByUserIdAndType(String userReportId, String documentType);
}
