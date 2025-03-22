package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.service.LeadService;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.service.AudioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})
public class LeadController {

    private final LeadService leadService;
    private final DocumentService documentService;
    private final AudioService audioService;

    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@RequestBody LeadRequest leadRequest) {
        LeadResponse leadResponse = leadService.createLead(leadRequest);
        return new ResponseEntity<>(leadResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLead(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        LeadResponse leadResponse = new LeadResponse();
        leadResponse.setId(leadEntity.getId());
        leadResponse.setAgentId(leadEntity.getAgentId());
        leadResponse.setName(leadEntity.getName());
        leadResponse.setEmail(leadEntity.getEmail());
        leadResponse.setDob(leadEntity.getDob());
        leadResponse.setGender(leadEntity.getGender());
        leadResponse.setAdharNumber(leadEntity.getAdharNumber());
        leadResponse.setPanNumber(leadEntity.getPanNumber());
        leadResponse.setVerifiedName(leadEntity.getVerifiedName());
        leadResponse.setVerifiedDob(leadEntity.getVerifiedDob());
        leadResponse.setVerifiedGender(leadEntity.getVerifiedGender());
        leadResponse.setVerifiedAdhar(leadEntity.getVerifiedAdhar());
        leadResponse.setVerifiedPan(leadEntity.getVerifiedPan());
        leadResponse.setAddress(leadEntity.getAddress());
        leadResponse.setPhoneNumber(leadEntity.getPhoneNumber());
        return ResponseEntity.ok(leadResponse);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<LeadEntity>> getLeadsByAgent(@PathVariable String agentId) {
        List<LeadEntity> leadEntities = leadService.getLeadsByAgentId(agentId);
        return ResponseEntity.ok(leadEntities);
    }

    //Combined lead insights end point
    @GetMapping("/{leadId}/insights")
    public ResponseEntity<List<InsightsEntity>> getLeadInsights(@PathVariable String leadId) {
        List<InsightsEntity> leadInsights = leadService.getLeadInsights(leadId);
        return ResponseEntity.ok(leadInsights);
    }
}