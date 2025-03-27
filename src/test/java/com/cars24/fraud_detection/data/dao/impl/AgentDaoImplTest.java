package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.entity.AgentEntity;
import com.cars24.fraud_detection.repository.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentDaoImplTest {

    @Mock
    private AgentRepository agentRepository;

    @InjectMocks
    private AgentDaoImpl agentDao;

    private AgentEntity agentEntity;

    @BeforeEach
    void setUp() {
        agentEntity = new AgentEntity();
        agentEntity.setId("agent123");
        agentEntity.setName("John Doe");
        agentEntity.setEmail("john@example.com");
    }

    @Test
    void testSaveAgent() {
        when(agentRepository.save(any(AgentEntity.class))).thenReturn(agentEntity);

        AgentEntity savedAgent = agentDao.saveAgent(agentEntity);

        assertNotNull(savedAgent);
        assertEquals("agent123", savedAgent.getId());
        verify(agentRepository).save(agentEntity);
    }

    @Test
    void testFindAgentById() {
        when(agentRepository.findById("agent123")).thenReturn(Optional.of(agentEntity));

        Optional<AgentEntity> foundAgent = agentDao.findAgentById("agent123");

        assertTrue(foundAgent.isPresent());
        assertEquals("agent123", foundAgent.get().getId());
    }

    @Test
    void testFindByEmail() {
        when(agentRepository.findByEmail("john@example.com")).thenReturn(Optional.of(agentEntity));

        Optional<AgentEntity> foundAgent = agentDao.findByEmail("john@example.com");

        assertTrue(foundAgent.isPresent());
        assertEquals("john@example.com", foundAgent.get().getEmail());
    }
}
