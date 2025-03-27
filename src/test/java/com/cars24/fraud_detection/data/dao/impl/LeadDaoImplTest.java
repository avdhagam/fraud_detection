package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import com.cars24.fraud_detection.repository.DocumentRepository;
import com.cars24.fraud_detection.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadDaoImplTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private AudioRepository audioRepository;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private LeadDaoImpl leadDao;

    private LeadEntity leadEntity;
    private AudioEntity audioEntity;
    private DocumentEntity documentEntity;

    @BeforeEach
    void setUp() {
        leadEntity = new LeadEntity();
        leadEntity.setId("lead123");

        audioEntity = new AudioEntity();
        audioEntity.setId("audio123");

        documentEntity = new DocumentEntity();
        documentEntity.setId("doc123");
    }

    @Test
    void testSaveLead() {
        when(leadRepository.save(any(LeadEntity.class))).thenReturn(leadEntity);

        LeadEntity savedLead = leadDao.saveLead(leadEntity);

        assertNotNull(savedLead);
        assertEquals("lead123", savedLead.getId());
    }

    @Test
    void testFindLeadById() {
        when(leadRepository.findById("lead123")).thenReturn(Optional.of(leadEntity));

        Optional<LeadEntity> foundLead = leadDao.findLeadById("lead123");

        assertTrue(foundLead.isPresent());
    }
}
