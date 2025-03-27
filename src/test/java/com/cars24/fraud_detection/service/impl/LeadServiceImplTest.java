package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.*;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.service.DocumentService;
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
class LeadServiceImplTest {

    @Mock
    private LeadDao leadDao;

    @Mock
    private FileDao fileDao;

    @Mock
    private DocumentService documentService;

    @Mock
    private AudioService audioService;

    @Mock
    private DocumentTypeConfig documentTypeConfig;

    @InjectMocks
    private LeadServiceImpl leadService;

    private LeadEntity leadEntity;
    private LeadRequest leadRequest;

    @BeforeEach
    void setUp() {
        leadEntity = new LeadEntity();
        leadEntity.setId("lead123");
        leadEntity.setAgentId("agent1");
        leadEntity.setName("John Doe");
        leadEntity.setEmail("johndoe@example.com");

        leadRequest = new LeadRequest();
        leadRequest.setAgentId("agent1");
        leadRequest.setName("John Doe");
        leadRequest.setEmail("johndoe@example.com");
    }

    @Test
    void testCreateLead_Success() {
        when(leadDao.saveLead(any(LeadEntity.class))).thenReturn(leadEntity);
        LeadResponse response = leadService.createLead(leadRequest);
        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        verify(leadDao, times(1)).saveLead(any(LeadEntity.class));
    }

    @Test
    void testGetLeadById_Success() {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.of(leadEntity));
        LeadEntity response = leadService.getLeadById("lead123");
        assertNotNull(response);
        assertEquals("lead123", response.getId());
    }

    @Test
    void testGetLeadById_NotFound() {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> leadService.getLeadById("lead123"));
        assertEquals("Lead not found with ID: lead123", exception.getMessage());
    }

    @Test
    void testGetLeadsByAgentId() {
        when(leadDao.findByAgentId("agent1")).thenReturn(List.of(leadEntity));
        List<LeadEntity> leads = leadService.getLeadsByAgentId("agent1");
        assertFalse(leads.isEmpty());
        assertEquals(1, leads.size());
    }

    @Test
    void testGetLeadName_Success() {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.of(leadEntity));
        String name = leadService.getLeadName("lead123");
        assertEquals("John Doe", name);
    }

    @Test
    void testGetLeadName_NotFound() {
        when(leadDao.findLeadById("lead123")).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> leadService.getLeadName("lead123"));
        assertEquals("Lead not found with ID: lead123", exception.getMessage());
    }
}
