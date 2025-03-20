package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
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
    public Optional<DocumentEntity> getDocumentById(String documentId) {
        return documentRepo.findById(documentId);
    }

}
