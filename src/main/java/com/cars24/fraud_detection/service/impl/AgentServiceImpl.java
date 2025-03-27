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
import com.cars24.fraud_detection.service.AgentService;
import com.cars24.fraud_detection.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentDao agentDao;
    private final PasswordEncoder passwordEncoder;
    private final LeadService leadService;
    @Override
    public AgentResponse registerAgent(AgentRequest agentRequest) {
        if (agentRequest.getName() == null || agentRequest.getName().isBlank()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty!");
        }

        AgentEntity agentEntity = new AgentEntity();
        agentEntity.setName(agentRequest.getName());
        agentEntity.setEmail(agentRequest.getEmail());
        agentEntity.setPassword(passwordEncoder.encode(agentRequest.getPassword())); // Hash the password

        AgentEntity savedAgent = agentDao.saveAgent(agentEntity);

        // Map the saved AgentEntity to AgentResponse
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setId(savedAgent.getId());
        agentResponse.setName(savedAgent.getName());
        agentResponse.setEmail(savedAgent.getEmail());

        return agentResponse;
    }

    @Override
    public LoginResponse loginAgent(LoginRequest loginRequest) {
        AgentEntity agentEntity = agentDao.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), agentEntity.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(agentEntity.getId());
        loginResponse.setName(agentEntity.getName());
        loginResponse.setEmail(agentEntity.getEmail());

        return loginResponse;
    }

    @Override
    public AgentEntity getAgentById(String agentId) {
        return agentDao.findAgentById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with ID: " + agentId));
    }

    @Override
    public AgentAnalysis analyzeAgent(String agentId) {
        List<LeadEntity> leads = leadService.getLeadsByAgentId(agentId);
        final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);
        logger.debug("Number of Leads: {}", leads.size());

        AgentAnalysis analysis = new AgentAnalysis();

        int totalReferenceCalls = 0;
        int acceptedReferenceCalls = 0;
        int rejectedReferenceCalls = 0;
        double totalAgentTalkTime = 0;

        double totalReferenceTalkTime = 0;
        int validReferenceCallCount = 0;

        int totalAadhars = 0;
        int acceptedAadhars = 0;
        int rejectedAadhars = 0;

        int totalPans = 0;
        int acceptedPans = 0;
        int rejectedPans = 0;


        for (LeadEntity lead : leads) {
            LeadResponse leadResponse = leadService.getLeadDetails(agentId, lead.getId());
            logger.debug("LeadResponse for lead ID {}: {}", lead.getId(), leadResponse);

            // Reference Call Analysis
            if (leadResponse != null && leadResponse.getReferenceCalls() != null) {
                int numReferenceCalls = leadResponse.getReferenceCalls().size();
                totalReferenceCalls += numReferenceCalls;
                logger.info("Added {} reference calls for lead ID {}.  Total reference calls now: {}.",
                        numReferenceCalls, lead.getId(), totalReferenceCalls);


                for (AudioEntity referenceCall : leadResponse.getReferenceCalls()) {
                    if ("accept".equalsIgnoreCase(referenceCall.getStatus())) {
                        acceptedReferenceCalls++;
                    } else if ("reject".equalsIgnoreCase(referenceCall.getStatus())) {
                        rejectedReferenceCalls++;
                    }

                    double agentTalkTime = 0;
                    double referenceTalkTime = 0;

                    if (referenceCall.getLlmExtraction() != null) {
                        Object transcriptObject = referenceCall.getLlmExtraction().get("transcript");

                        if (transcriptObject instanceof List) {
                            List<Map<String, Object>> transcript = (List<Map<String, Object>>) transcriptObject;
                            if (transcript != null) {

                                validReferenceCallCount++;

                                for (Map<String, Object> utterance : transcript) {

                                    String speaker = (String) utterance.get("speaker");
                                    Double startTime = (Double) utterance.get("start_time");
                                    Double endTime = (Double) utterance.get("end_time");

                                    if (startTime != null && endTime != null) {
                                        double duration = endTime - startTime;

                                        if (speaker != null && speaker.startsWith("SPEAKER_00")) {
                                            agentTalkTime += duration;
                                        } else {
                                            referenceTalkTime += duration;
                                        }
                                    }
                                }
                                totalAgentTalkTime += agentTalkTime;
                                totalReferenceTalkTime += referenceTalkTime;
                            }
                        }
                    }
                }
            }

            //Aadhar Analysis
            if (leadResponse != null && leadResponse.getAadhaar() != null) { //ADDED NULL CHECK HERE
                totalAadhars += leadResponse.getAadhaar().size();
                for (DocumentEntity aadhar : leadResponse.getAadhaar()) {
                    if (aadhar.getDecision() != null && "REJECT".equalsIgnoreCase(aadhar.getDecision())) {
                        rejectedAadhars++;
                    } else {
                        acceptedAadhars++;
                    }
                }
            }

            //PAN Analysis
            if (leadResponse != null && leadResponse.getPan() != null) { //ADDED NULL CHECK HERE
                totalPans += leadResponse.getPan().size();
                for (DocumentEntity pan : leadResponse.getPan()) {
                    if (pan.getDecision() != null && "REJECT".equalsIgnoreCase(pan.getDecision())) {
                        rejectedPans++;
                    } else {
                        acceptedPans++;
                    }
                }
            }
        }

        if (validReferenceCallCount > 0) {
            analysis.setAverageAgentTalkTime(totalAgentTalkTime / validReferenceCallCount);
            analysis.setAverageReferenceTalkTime(totalReferenceTalkTime / validReferenceCallCount);
        } else {
            analysis.setAverageAgentTalkTime(0);
            analysis.setAverageReferenceTalkTime(0);
        }

        analysis.setTotalReferenceCalls(totalReferenceCalls);
        analysis.setAcceptedReferenceCalls(acceptedReferenceCalls);
        analysis.setRejectedReferenceCalls(rejectedReferenceCalls);

        analysis.setTotalAadhars(totalAadhars);
        analysis.setAcceptedAadhars(acceptedAadhars);
        analysis.setRejectedAadhars(rejectedAadhars);

        analysis.setTotalPans(totalPans);
        analysis.setAcceptedPans(acceptedPans);
        analysis.setRejectedPans(rejectedPans);

        logger.info("Total Reference Calls: {}", totalReferenceCalls);
        return analysis;
    }
}