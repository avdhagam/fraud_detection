package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.service.LeadService;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.config.DocumentTypeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadDao leadDao;
    private final DocumentService documentService;
    private final AudioService audioService;
    private final DocumentTypeConfig documentTypeConfig;

    @Override
    public LeadResponse createLead(LeadRequest leadRequest) {
        LeadEntity leadEntity = new LeadEntity();
        leadEntity.setAgentId(leadRequest.getAgentId());
        leadEntity.setName(leadRequest.getName());
        leadEntity.setEmail(leadRequest.getEmail());
        leadEntity.setDob(leadRequest.getDob());
        leadEntity.setGender(leadRequest.getGender());
        leadEntity.setAdharNumber(leadRequest.getAdharNumber());
        leadEntity.setPanNumber(leadRequest.getPanNumber());
        leadEntity.setVerifiedName(leadRequest.getVerifiedName());
        leadEntity.setVerifiedDob(leadRequest.getVerifiedDob());
        leadEntity.setVerifiedGender(leadRequest.getVerifiedGender());
        leadEntity.setVerifiedAdhar(leadRequest.getVerifiedAdhar());
        leadEntity.setVerifiedPan(leadRequest.getVerifiedPan());
        leadEntity.setAddress(leadRequest.getAddress());
        leadEntity.setPhoneNumber(leadRequest.getPhoneNumber());

        LeadEntity savedLead = leadDao.saveLead(leadEntity);

        LeadResponse leadResponse = new LeadResponse();
        leadResponse.setId(savedLead.getId());
        leadResponse.setAgentId(savedLead.getAgentId());
        leadResponse.setName(savedLead.getName());
        leadResponse.setEmail(savedLead.getEmail());
        leadResponse.setDob(savedLead.getDob());
        leadResponse.setGender(savedLead.getGender());
        leadResponse.setAdharNumber(savedLead.getAdharNumber());
        leadResponse.setPanNumber(savedLead.getPanNumber());
        leadResponse.setVerifiedName(savedLead.getVerifiedName());
        leadResponse.setVerifiedDob(savedLead.getVerifiedDob());
        leadResponse.setVerifiedGender(savedLead.getVerifiedGender());
        leadResponse.setVerifiedAdhar(savedLead.getVerifiedAdhar());
        leadResponse.setVerifiedPan(savedLead.getVerifiedPan());
        leadResponse.setAddress(savedLead.getAddress());
        leadResponse.setPhoneNumber(savedLead.getPhoneNumber());

        return leadResponse;
    }

    @Override
    public LeadEntity getLeadById(String leadId) {
        return leadDao.findLeadById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found with ID: " + leadId));
    }

    @Override
    public List<LeadEntity> getLeadsByAgentId(String agentId) {
        return leadDao.findByAgentId(agentId);
    }

    @Override
    public List<InsightsEntity> getLeadInsights(String leadId) {
        List<InsightsEntity> overallInsights = new ArrayList<>();
        Map<String, String> docConfig = documentTypeConfig.getMapping();

        for (Map.Entry<String, String> entry : docConfig.entrySet()) {
            String documentType = entry.getKey();
            String documentName = entry.getValue();

            if (documentType.equals("REFERENCE_CALL")) {
                // Fetch audio insights for the lead
                List<AudioEntity> audioEntities = audioService.getAudiosByLeadId(leadId);
                if (audioEntities != null && !audioEntities.isEmpty()) {
                    for (AudioEntity audio : audioEntities) {
                        overallInsights.add(createAudioInsightsEntity(audio, documentName));
                    }
                } else {
                    // Create a "Pending" insight if no audio is found
                    overallInsights.add(createPendingAudioInsightsEntity(leadId, documentType, documentName));
                }
            } else {
                // Fetch document insights for the lead
                List<DocumentEntity> documentEntities = documentService.getDocumentsByLeadId(leadId);
                if (documentEntities != null && !documentEntities.isEmpty()) {
                    for (DocumentEntity document : documentEntities) {
                        overallInsights.add(createDocumentInsightsEntity(document, documentName));
                    }
                } else {
                    // Create a "Pending" insight if no document is found
                    overallInsights.add(createPendingDocumentInsightsEntity(leadId, documentType, documentName));
                }
            }
        }

        return overallInsights;
    }

    private InsightsEntity createDocumentInsightsEntity(DocumentEntity document, String documentName) {
        return InsightsEntity.builder()
                .leadId(document.getLeadId())
                .doctype(document.getDocumentType())
                .documentName(documentName)
                .status(document.getStatus())
                .score(document.getFinalRiskScore())
                .description(document.getDecision())
                .uploadedAt(document.getTimestamp())
                .build();
    }

    private InsightsEntity createAudioInsightsEntity(AudioEntity audio, String documentName) {
        return InsightsEntity.builder()
                .leadId(audio.getLeadId())
                .doctype(audio.getDocumentType())
                .documentName(documentName)
                .status(audio.getStatus())
                .score((double) audio.getOverallScore())
                .description(audio.getStatus())
                .uploadedAt(audio.getTimestamp())
                .build();
    }

    private InsightsEntity createPendingDocumentInsightsEntity(String leadId, String documentType, String documentName) {
        return InsightsEntity.builder()
                .leadId(leadId)
                .doctype(documentType)
                .documentName(documentName)
                .status("Pending")
                .score(0.0)
                .description("Document not Uploaded")
                .uploadedAt(null)
                .build();
    }

    private InsightsEntity createPendingAudioInsightsEntity(String leadId, String documentType, String documentName) {
        return InsightsEntity.builder()
                .leadId(leadId)
                .doctype(documentType)
                .documentName(documentName)
                .status("Pending")
                .score(0.0)
                .description("Audio not Uploaded")
                .uploadedAt(null)
                .build();
    }
}