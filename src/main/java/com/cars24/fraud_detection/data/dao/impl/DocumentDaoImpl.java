package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentDaoImpl implements DocumentDao {

    private final DocumentRepository documentRepo;

    @Override
    public void saveDocument(DocumentEntity document) {
        if (document.getUserId()== null) {
            throw new IllegalArgumentException("User ID cannot be null when saving document!");
        }
        documentRepo.save(document);
    }

    @Override
    public void updateDocument(DocumentEntity document) {
        if (document.getId() == null) {
            throw new IllegalArgumentException("Document ID cannot be null for an update operation!");
        }
        System.out.println("Updating DocumentEntity with ID: " + document.getId());
        documentRepo.save(document);
        System.out.println("Updated DocumentEntity: " + document);
    }
    @Override
    public Optional<DocumentEntity> getDocumentById(String documentId) {
        return documentRepo.findById(documentId);
    }

    public Optional<DocumentEntity> findFirstByUserIdAndDocumentType(String userId, String documentType) {
        return documentRepo.findFirstByUserIdAndDocumentType(userId, documentType);
    }

    @Override
    public List<DocumentEntity> getRecentDocumentsByUserId(String userId, int limit) {
        return documentRepo.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, limit));
    }

    @Override
    public Optional<DocumentEntity> findByUserIdAndDocumentType(String userReportId, String documentType) {
        return documentRepo.findByUserIdAndDocumentType(userReportId, documentType);
    }
}
