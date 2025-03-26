package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AgentRequest;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.response.AgentAnalysis;
import com.cars24.fraud_detection.data.response.AgentResponse;
import com.cars24.fraud_detection.data.response.LoginResponse;
import com.cars24.fraud_detection.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/register")
    public ResponseEntity<AgentResponse> registerAgent(@Valid @RequestBody AgentRequest agentRequest) {
        AgentResponse agentResponse = agentService.registerAgent(agentRequest);
        return ResponseEntity.ok(agentResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginAgent(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = agentService.loginAgent(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    //Optional: Get Agent by ID
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentResponse> getAgent(@PathVariable String agentId) {
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setId(agentService.getAgentById(agentId).getId());
        agentResponse.setName(agentService.getAgentById(agentId).getName());
        agentResponse.setEmail(agentService.getAgentById(agentId).getEmail());
        return ResponseEntity.ok(agentResponse);
    }

    @GetMapping("/{agentId}/analysis")
    public ResponseEntity<AgentAnalysis> getAgentAnalysis(@PathVariable String agentId) {
        AgentAnalysis analysis = agentService.analyzeAgent(agentId);
        return ResponseEntity.ok(analysis);
    }

}