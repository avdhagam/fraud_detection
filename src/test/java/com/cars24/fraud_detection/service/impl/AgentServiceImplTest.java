package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.AgentDao;
import com.cars24.fraud_detection.data.entity.AgentEntity;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.request.AgentRequest;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.response.AgentAnalysis;
import com.cars24.fraud_detection.data.response.AgentResponse;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.data.response.LoginResponse;
import com.cars24.fraud_detection.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock
    private AgentDao agentDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LeadService leadService;

    @InjectMocks
    private AgentServiceImpl agentService;

    private AgentRequest agentRequest;
    private AgentEntity agentEntity;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        agentRequest = new AgentRequest();
        agentRequest.setName("John Doe");
        agentRequest.setEmail("john.doe@example.com");
        agentRequest.setPassword("password123");

        agentEntity = new AgentEntity();
        agentEntity.setId("agent123");
        agentEntity.setName("John Doe");
        agentEntity.setEmail("john.doe@example.com");
        agentEntity.setPassword("encodedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterAgent_Success() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(agentDao.saveAgent(any(AgentEntity.class))).thenReturn(agentEntity);

        AgentResponse response = agentService.registerAgent(agentRequest);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());

        verify(agentDao).saveAgent(any(AgentEntity.class));
    }

    @Test
    void testLoginAgent_Success() {
        when(agentDao.findByEmail(anyString())).thenReturn(Optional.of(agentEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginResponse response = agentService.loginAgent(loginRequest);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
    }

    @Test
    void testLoginAgent_InvalidCredentials() {
        when(agentDao.findByEmail(anyString())).thenReturn(Optional.of(agentEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> agentService.loginAgent(loginRequest));
    }

    @Test
    void testGetAgentById_Found() {
        when(agentDao.findAgentById("agent123")).thenReturn(Optional.of(agentEntity));

        AgentEntity foundAgent = agentService.getAgentById("agent123");

        assertNotNull(foundAgent);
        assertEquals("agent123", foundAgent.getId());
    }

    @Test
    void testGetAgentById_NotFound() {
        when(agentDao.findAgentById("agent123")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> agentService.getAgentById("agent123"));
    }
}
