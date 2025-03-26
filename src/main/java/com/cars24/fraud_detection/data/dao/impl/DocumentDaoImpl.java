package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.DocumentDao;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentDaoImpl implements DocumentDao {

    private final DocumentRepository documentRepository;

    @Override
    public DocumentEntity saveDocument(DocumentEntity document) {
        return documentRepository.save(document);
    }

    @Override
    public DocumentEntity updateDocument(DocumentEntity document) {
        return documentRepository.save(document);
    }

    @Override
    public Optional<DocumentEntity> getDocumentById(String documentId) {
        return documentRepository.findById(documentId);
    }

    @Override
    public List<DocumentEntity> findByLeadId(String leadId) {
        return documentRepository.findByLeadId(leadId);
    }

    @Override
    public List<DocumentEntity> getRecentDocumentsByLeadId(String leadId, int limit) {
        return documentRepository.findByLeadIdOrderByTimestampDesc(leadId, PageRequest.of(0, limit));
    }



    @Override
    public List<DocumentEntity> findByLeadIdAndDocumentType(String leadId, String documentType) {
        return documentRepository.findByLeadIdAndDocumentType(leadId, documentType);
    }

    public Optional<DocumentEntity> findByIdAndDocumentType(String documentId, String documentType) {
        return documentRepository.findByIdAndDocumentType(documentId, documentType);
    }

    @Override
    public List<DocumentEntity> getRecentDocumentsByLeadIdAndType(String leadId, String doctype, Pageable pageable) {
        return documentRepository.getRecentDocumentsByLeadIdAndType(leadId, doctype, pageable);
    }
}