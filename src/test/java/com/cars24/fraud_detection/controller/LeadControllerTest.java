package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    @Mock
    private LeadService leadService;

    @InjectMocks
    private LeadController leadController;

    private LeadEntity leadEntity;
    private LeadResponse leadResponse;
    private LeadRequest leadRequest;
    private InsightsEntity insightsEntity;

    @BeforeEach
    void setUp() {
        leadEntity = new LeadEntity();
        leadEntity.setId("lead123");
        leadEntity.setName("Test Lead");
        leadEntity.setEmail("lead@test.com");

        leadResponse = new LeadResponse(leadEntity);
        leadRequest = new LeadRequest();
        insightsEntity = InsightsEntity.builder()
                .leadId("lead123")
                .id("insight123")
                .doctype("Aadhaar")
                .documentName("Aadhaar Card")
                .status("Verified")
                .score(0.9)
                .description("Valid document")
                .uploadedAt(LocalDateTime.now())
                .build();

    }

    @Test
    void testCreateLead() {
        when(leadService.createLead(any(LeadRequest.class))).thenReturn(leadResponse);

        ResponseEntity<LeadResponse> response = leadController.createLead(leadRequest);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(leadService).createLead(any(LeadRequest.class));
    }

    @Test
    void testGetLead_Success() {
        when(leadService.getLeadById("lead123")).thenReturn(leadEntity);

        ResponseEntity<LeadResponse> response = leadController.getLead("lead123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("lead123", response.getBody().getId());
    }

    @Test
    void testGetLeadsByAgent() {
        when(leadService.getLeadsByAgentId("agent1")).thenReturn(Collections.singletonList(leadEntity));

        ResponseEntity<List<LeadEntity>> response = leadController.getLeadsByAgent("agent1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetLeadInsights() {
        when(leadService.getLeadInsights("lead123")).thenReturn(Collections.singletonList(insightsEntity));

        ResponseEntity<List<InsightsEntity>> response = leadController.getLeadInsights("lead123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetLeadName_Success() {
        when(leadService.getLeadById("lead123")).thenReturn(leadEntity);

        ResponseEntity<String> response = leadController.getLeadName("lead123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Test Lead", response.getBody());
    }

    @Test
    void testGetLeadName_NotFound() {
        when(leadService.getLeadById("lead123")).thenReturn(null);

        ResponseEntity<String> response = leadController.getLeadName("lead123");

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetLeadFiles() {
        when(leadService.getLeadDetails("agent1", "lead123")).thenReturn(leadResponse);

        LeadResponse response = leadController.getLeadFiles("lead123", "agent1");

        assertNotNull(response);
        assertEquals("lead123", response.getId());
    }

    @Test
    void testGetLeadDetailsWithActiveFiles() {
        when(leadService.getActiveLeadDetails("agent1", "lead123")).thenReturn(leadResponse);

        LeadResponse response = leadController.getLeadDetailsWithActiveFiles("agent1", "lead123");

        assertNotNull(response);
        assertEquals("lead123", response.getId());
    }
}
