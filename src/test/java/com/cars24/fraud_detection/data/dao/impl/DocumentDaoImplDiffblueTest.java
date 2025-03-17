package com.cars24.fraud_detection.data.dao.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.repository.DocumentRepository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {DocumentDaoImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class DocumentDaoImplDiffblueTest {
    @Autowired
    private DocumentDaoImpl documentDaoImpl;

    @MockBean
    private DocumentRepository documentRepository;

    /**
     * Test {@link DocumentDaoImpl#saveDocument(DocumentEntity)}.
     * <p>
     * Method under test: {@link DocumentDaoImpl#saveDocument(DocumentEntity)}
     */
    @Test
    @DisplayName("Test saveDocument(DocumentEntity)")
    @Tag("MaintainedByDiffblue")
    void testSaveDocument() {
        // Arrange
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDecision("Decision");
        documentEntity.setFileName("foo.txt");
        documentEntity.setFilePath("/directory/foo.txt");
        documentEntity.setFinalRiskScore(10.0d);
        documentEntity.setId("42");
        documentEntity.setNextSteps("Next Steps");
        documentEntity.setRemarks("Remarks");
        documentEntity.setRiskLevel("Risk Level");
        documentEntity.setStatus("Status");
        documentEntity.setUserId("42");
        when(documentRepository.save(Mockito.<DocumentEntity>any())).thenReturn(documentEntity);

        DocumentEntity document = new DocumentEntity();
        document.setDecision("Decision");
        document.setFileName("foo.txt");
        document.setFilePath("/directory/foo.txt");
        document.setFinalRiskScore(10.0d);
        document.setId("42");
        document.setNextSteps("Next Steps");
        document.setRemarks("Remarks");
        document.setRiskLevel("Risk Level");
        document.setStatus("Status");
        document.setUserId("42");

        // Act
        documentDaoImpl.saveDocument(document);

        // Assert
        verify(documentRepository).save(isA(DocumentEntity.class));
    }

    /**
     * Test {@link DocumentDaoImpl#getDocumentById(String)}.
     * <p>
     * Method under test: {@link DocumentDaoImpl#getDocumentById(String)}
     */
    @Test
    @DisplayName("Test getDocumentById(String)")
    @Tag("MaintainedByDiffblue")
    void testGetDocumentById() {
        // Arrange
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDecision("Decision");
        documentEntity.setFileName("foo.txt");
        documentEntity.setFilePath("/directory/foo.txt");
        documentEntity.setFinalRiskScore(10.0d);
        documentEntity.setId("42");
        documentEntity.setNextSteps("Next Steps");
        documentEntity.setRemarks("Remarks");
        documentEntity.setRiskLevel("Risk Level");
        documentEntity.setStatus("Status");
        documentEntity.setUserId("42");
        Optional<DocumentEntity> ofResult = Optional.of(documentEntity);
        when(documentRepository.findById(Mockito.<String>any())).thenReturn(ofResult);

        // Act
        Optional<DocumentEntity> actualDocumentById = documentDaoImpl.getDocumentById("42");

        // Assert
        verify(documentRepository).findById(eq("42"));
        assertSame(ofResult, actualDocumentById);
    }
}
