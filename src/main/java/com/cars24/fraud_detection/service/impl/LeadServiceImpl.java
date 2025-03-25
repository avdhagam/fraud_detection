package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.FileDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.*;
import com.cars24.fraud_detection.data.request.LeadRequest;
import com.cars24.fraud_detection.data.response.LeadNameEmail;
import com.cars24.fraud_detection.data.response.LeadResponse;
import com.cars24.fraud_detection.service.LeadService;
import com.cars24.fraud_detection.service.DocumentService;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.config.DocumentTypeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadDao leadDao;
    private final FileDao fileDao;
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
        leadEntity.setAddress(leadRequest.getAddress());
        leadEntity.setPhoneNumber(leadRequest.getPhoneNumber());
        leadEntity.setReferenceName(leadRequest.getReferenceName());
        leadEntity.setRelationToSubject(leadRequest.getRelationToSubject());
        leadEntity.setSubjectOccupation(leadRequest.getSubjectOccupation());
        leadEntity.setFatherName(leadRequest.getFatherName());
       // leadEntity.setDocType(leadRequest.getDocType());
        leadEntity.setCreatedAt(LocalDate.now().atStartOfDay());

        LeadEntity savedLead = leadDao.saveLead(leadEntity);

        return new LeadResponse(savedLead);
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
    public String getLeadName(String leadId) {
        LeadEntity leadEntity = getLeadById(leadId);  // Reuse existing method
        return leadEntity.getName();
    }

    @Override
    public List<LeadNameEmail> getLeadNameEmailByAgentId(String agentId) {
        List<LeadEntity> leadEntities = leadDao.findByAgentId(agentId);
        return leadEntities.stream()
                .map(lead -> new LeadNameEmail(lead.getId(), lead.getName(), lead.getEmail()))
                .collect(Collectors.toList());
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
                .description(audio.getStatus()).uploadedAt(audio.getTimestamp())
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

    @Override
    public LeadResponse getLeadDetails(String agentId, String leadId) {

        LeadEntity leadEntity = leadDao.findByAgentIdAndId(agentId,leadId);

        List<AudioEntity> referenceCalls = leadDao.findAudioByAgentAndLead(agentId,leadId);
        List<DocumentEntity> documents = leadDao.findDocumentsByAgentAndLead(agentId,leadId);



        List<DocumentEntity> aadhaarDocs = documents.stream()
                .filter(doc -> "AADHAAR".equalsIgnoreCase(doc.getDocumentType()))
                .collect(Collectors.toList());

        List<DocumentEntity> panDocs = documents.stream()
                .filter(doc -> "PAN".equalsIgnoreCase(doc.getDocumentType()))
                .collect(Collectors.toList());



        LeadResponse response = new LeadResponse(leadEntity);
        response.setReferenceCalls(referenceCalls);
        response.setAadhaar(aadhaarDocs);
        response.setPan(panDocs);

        return response;
   }

    @Override
    public LeadResponse getActiveLeadDetails(String agentId, String leadId) {
        LeadEntity leadEntity = leadDao.findByAgentIdAndId(agentId, leadId);

        // ✅ Fetch only active files
        List<FileEntity> activeFiles = fileDao.findByAgentIdAndLeadIdAndIsActive(agentId, leadId, true);

        // ✅ Extract active file types mapped to agentId & leadId
        Set<String> activeFileTypes = activeFiles.stream()
                .map(FileEntity::getFileType)
                .collect(Collectors.toSet());

        // ✅ Fetch all Audio and Documents
        List<AudioEntity> allAudio = leadDao.findAudioByAgentAndLead(agentId, leadId);
        List<DocumentEntity> allDocuments = leadDao.findDocumentsByAgentAndLead(agentId, leadId);

        // ✅ Filter only active AudioEntities that match agentId, leadId & fileType
        List<AudioEntity> activeAudio = allAudio.stream()
                .filter(audio -> activeFileTypes.contains(audio.getDocumentType()) &&
                        activeFiles.stream().anyMatch(f ->
                                f.getAgentId().equals(agentId) &&
                                        f.getLeadId().equals(leadId) &&
                                        f.getFileType().equals(audio.getDocumentType())))
                .collect(Collectors.toList());

        // ✅ Filter only active DocumentEntities that match agentId, leadId & fileType
        List<DocumentEntity> activeDocuments = allDocuments.stream()
                .filter(doc -> activeFileTypes.contains(doc.getDocumentType()) &&
                        activeFiles.stream().anyMatch(f ->
                                f.getAgentId().equals(agentId) &&
                                        f.getLeadId().equals(leadId) &&
                                        f.getFileType().equals(doc.getDocumentType())))
                .collect(Collectors.toList());

        // ✅ Separate Aadhaar and PAN documents from active documents
        List<DocumentEntity> aadhaarDocs = activeDocuments.stream()
                .filter(doc -> "AADHAAR".equalsIgnoreCase(doc.getDocumentType()))
                .collect(Collectors.toList());

        List<DocumentEntity> panDocs = activeDocuments.stream()
                .filter(doc -> "PAN".equalsIgnoreCase(doc.getDocumentType()))
                .collect(Collectors.toList());

        // ✅ Build response
        LeadResponse response = new LeadResponse(leadEntity);
        response.setReferenceCalls(activeAudio);
        response.setAadhaar(aadhaarDocs);
        response.setPan(panDocs);

        return response;
    }






}