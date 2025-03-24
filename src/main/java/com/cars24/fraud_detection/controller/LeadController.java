package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.*;
import com.cars24.fraud_detection.service.LeadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS})
public class LeadController {

    private final LeadService leadService;


    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@RequestBody LeadRequest leadRequest) {
        LeadResponse leadResponse = leadService.createLead(leadRequest);
        return new ResponseEntity<>(leadResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLead(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        LeadResponse leadResponse = new LeadResponse(leadEntity);
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

    @GetMapping("/{leadId}/aadhaar")
    public ResponseEntity<AadhaarGroundTruth> getAadhaarGroundTruth(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        if (leadEntity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AadhaarGroundTruth.fromLeadEntity(leadEntity));
    }

    @GetMapping("/{leadId}/audio")
    public ResponseEntity<AudioGroundTruth> getAudioGroundTruth(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        if (leadEntity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AudioGroundTruth.fromLeadEntity(leadEntity));
    }

    @GetMapping("/{leadId}/pan")
    public ResponseEntity<PanGroundTruth> getPanGroundTruth(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        if (leadEntity == null) {
            return ResponseEntity.notFound().build();
        }
        PanGroundTruth panGroundTruth = PanGroundTruth.fromLeadEntity(leadEntity);
        return ResponseEntity.ok(panGroundTruth);
    }
    @GetMapping("/{leadId}/name")
    public ResponseEntity<String> getLeadName(@PathVariable String leadId) {
        LeadEntity leadEntity = leadService.getLeadById(leadId);
        if (leadEntity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leadEntity.getName());  // Just return the name
    }
    @GetMapping("/agent/{agentId}/name-email")
    public ResponseEntity<List<LeadNameEmail>> getLeadNameEmailByAgent(@PathVariable String agentId) {
        List<LeadEntity> leadEntities = leadService.getLeadsByAgentId(agentId);
        List<LeadNameEmail> leadNameEmails = leadEntities.stream()
                .map(lead -> new LeadNameEmail(lead.getId(), lead.getName(), lead.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(leadNameEmails);
    }
}