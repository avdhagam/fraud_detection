package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AgentRequest;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.response.AgentAnalysis;
import com.cars24.fraud_detection.data.response.AgentResponse;
import com.cars24.fraud_detection.data.response.LoginResponse;
import com.cars24.fraud_detection.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    @InjectMocks
    private AgentController agentController;

    private AgentRequest agentRequest;
    private AgentResponse agentResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private AgentAnalysis agentAnalysis;

    @BeforeEach
    void setUp() {
        agentRequest = new AgentRequest();
        agentResponse = new AgentResponse();
        loginRequest = new LoginRequest();
        loginResponse = new LoginResponse();
        agentAnalysis = new AgentAnalysis();
    }

    @Test
    void testRegisterAgent() {
        when(agentService.registerAgent(any(AgentRequest.class))).thenReturn(agentResponse);

        ResponseEntity<AgentResponse> response = agentController.registerAgent(agentRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(agentService).registerAgent(any(AgentRequest.class));
    }

    @Test
    void testLoginAgent() {
        when(agentService.loginAgent(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = agentController.loginAgent(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(agentService).loginAgent(any(LoginRequest.class));
    }

    @Test
    void testGetAgentAnalysis() {
        when(agentService.analyzeAgent("123")).thenReturn(agentAnalysis);

        ResponseEntity<AgentAnalysis> response = agentController.getAgentAnalysis("123");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(agentService).analyzeAgent("123");
    }
}
