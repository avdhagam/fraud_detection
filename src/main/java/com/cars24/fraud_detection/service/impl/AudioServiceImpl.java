//package com.cars24.fraud_detection.service.impl;
//
//import com.cars24.fraud_detection.config.DocumentTypeConfig;
//import com.cars24.fraud_detection.data.dao.AudioDao;
//import com.cars24.fraud_detection.data.dao.LeadDao;
//import com.cars24.fraud_detection.data.entity.AudioEntity;
//import com.cars24.fraud_detection.data.entity.InsightsEntity;
//import com.cars24.fraud_detection.data.entity.LeadEntity;
//import com.cars24.fraud_detection.data.request.AudioRequest;
//import com.cars24.fraud_detection.data.response.AudioResponse;
//import com.cars24.fraud_detection.exception.AudioProcessingException;
//import com.cars24.fraud_detection.service.AudioService;
//import com.cars24.fraud_detection.workflow.WorkflowInitiator;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AudioServiceImpl implements AudioService {
//
//    private static final String AUDIO_STORAGE_PATH = "audio_storage/"; // Correct path
//    private final AudioDao audioDao;
//    private final LeadDao leadDao;
//    private final WorkflowInitiator workflowInitiator;
//    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());
//    private final DocumentTypeConfig documentTypeConfig;
//    @Override
//    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException {
//        logger.info("Received AudioRequest object: " + audioRequest);
//
//        // 1. Validate Lead Existence
//        LeadEntity lead = leadDao.findLeadById(audioRequest.getLeadId())
//                .orElseThrow(() -> new AudioProcessingException("Lead not found with ID: " + audioRequest.getLeadId()));
//
//        // 3. Process Audio using Workflow
//        AudioResponse audioResponse = workflowInitiator.processAudio(audioRequest);
//        if (audioResponse == null) {
//            throw new AudioProcessingException("Audio processing failed");
//        }
//
//        // 4. Create AudioEntity
//        AudioEntity audioEntity = new AudioEntity();
//        audioEntity.setId(audioRequest.getUuid().toString()); // Use UUID
//        audioEntity.setLeadId(audioRequest.getLeadId());
//        audioEntity.setAgentId(audioRequest.getAgentId());
//        audioEntity.setDocumentType(audioRequest.getDocumentType());
//        audioEntity.setLlmExtraction(audioResponse.getLlmExtraction());
//        audioEntity.setTranscript(audioResponse.getTranscript());
//        audioEntity.setReferenceName(audioResponse.getReferenceName());
//        audioEntity.setSubjectName(audioResponse.getSubjectName());
//        audioEntity.setSubjectAddress(audioResponse.getSubjectAddress());
//        audioEntity.setRelationToSubject(audioResponse.getRelationToSubject());
//        audioEntity.setSubjectOccupation(audioResponse.getSubjectOccupation());
//        audioEntity.setOverallScore(audioResponse.getOverallScore());
//        audioEntity.setExplanation(audioResponse.getExplanation());
//        audioEntity.setFieldByFieldScores(audioResponse.getFieldByFieldScores());
//        audioEntity.setAudioAnalysis(audioResponse.getAudioAnalysis());
//        audioEntity.setStatus(audioResponse.getStatus());
//        audioEntity.setTimestamp(LocalDateTime.now());
//
//        // 5. Save AudioEntity
//        audioDao.saveAudio(audioEntity);
//        logger.info("Saved AudioEntity object to database with ID: " + audioEntity.getId());
//
//        // 6. Return response with UUID
//        audioResponse.setId(audioEntity.getId()); // Set the generated UUID
//        return audioResponse;
//    }
//
//    @Override
//    public AudioResponse getAudioResults(String audioId) throws AudioProcessingException {
//        logger.info("Fetching audio analysis for ID: " + audioId);
//
//        Optional<AudioEntity> audioEntityOpt = audioDao.getAudioById(audioId);
//        if (audioEntityOpt.isEmpty()) {
//            logger.warning("Audio analysis not found for ID: " + audioId);
//            throw new AudioProcessingException("Audio analysis not found for ID: " + audioId);
//        }
//
//        AudioEntity audioEntity = audioEntityOpt.get();
//        logger.info("Fetched audio entity: " + audioEntity);
//
//        AudioResponse response = new AudioResponse();
//        response.setId(audioEntity.getId());
//        response.setLeadId(audioEntity.getLeadId());
//        response.setDocumentType(audioEntity.getDocumentType());
//        response.setLlmExtraction(audioEntity.getLlmExtraction());
//        response.setTranscript(audioEntity.getTranscript());
//        response.setReferenceName(audioEntity.getReferenceName());
//        response.setSubjectName(audioEntity.getSubjectName());
//        response.setSubjectAddress(audioEntity.getSubjectAddress());
//        response.setRelationToSubject(audioEntity.getRelationToSubject());
//        response.setSubjectOccupation(audioEntity.getSubjectOccupation());
//        response.setOverallScore(audioEntity.getOverallScore());
//        response.setExplanation(audioEntity.getExplanation());
//        response.setFieldByFieldScores(audioEntity.getFieldByFieldScores());
//        response.setAudioAnalysis(audioEntity.getAudioAnalysis());
//        response.setStatus(audioEntity.getStatus());
//
//        return response;
//    }
//
//    @Override
//    public List<AudioEntity> getAudiosByLeadId(String leadId) {
//        return audioDao.findByLeadId(leadId);
//    }
//
//    @Override
//    public List<String> getRecentAudios(String leadId, int limit) {
//        log.info("Fetching recent audio UUIDs for lead ID: {} with limit: {}", leadId, limit);
//
//        List<AudioEntity> audios = audioDao.getRecentAudiosByLeadId(leadId, limit);
//
//        log.info("Total audios fetched: " + audios.size());
//
//        return audios.stream()
//                .map(AudioEntity::getId)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public String saveAudio(MultipartFile file, String uuid) throws AudioProcessingException {
//        try {
//            // Check if file is empty
//            if (file.isEmpty()) {
//                logger.severe("Failed to save audio file: File is empty");
//                throw new AudioProcessingException("Failed to store audio file: File is empty");
//            }
//
//            // Ensure the storage directory exists
//            Path storagePath = Paths.get(AUDIO_STORAGE_PATH);
//            Files.createDirectories(storagePath);
//
//            // Use the correct UUID from audioRequest
//            String uniqueFileName = uuid + ".mp3";
//
//            // Properly construct path using Path API
//            Path destinationFile = storagePath.resolve(uniqueFileName);
//
//            logger.info("Attempting to save file to: " + destinationFile.toString());
//
//            // Save the file to disk
//            Files.copy(file.getInputStream(), destinationFile);
//
//            logger.info("Audio file saved successfully to " + destinationFile.toString());
//
//            return destinationFile.toString();
//        } catch (IOException e) {
//            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
//            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ResponseEntity<FileSystemResource> getAudioFile(String audioId) {
//        try {
//            // Construct the file path
//            Path filePath = Paths.get(AUDIO_STORAGE_PATH, audioId + ".mp3");
//            File audioFile = filePath.toFile();
//
//            if (!audioFile.exists()) {
//                log.warn("Audio file not found for id: {}", audioId);
//                return ResponseEntity.notFound().build();
//            }
//
//            FileSystemResource resource = new FileSystemResource(audioFile);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("audio/mpeg")); // Set content type to audio/mpeg
//            headers.setContentLength(audioFile.length());
//            headers.setContentDispositionFormData("attachment", audioId + ".mp3"); // Optional:  Suggest a filename
//
//            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//
//        } catch (Exception e) {
//            log.error("Error retrieving audio file for id: {}", audioId, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @Override
//    public InsightsEntity getAudioInsights(String audioId) throws AudioProcessingException {
//        AudioEntity audioEntity = audioDao.getAudioById(audioId)
//                .orElseThrow(() -> new AudioProcessingException("Audio not found for ID: " + audioId));
//
//        String documentName = documentTypeConfig.getMapping().get(audioEntity.getDocumentType());
//
//        return InsightsEntity.builder()
//                .leadId(audioEntity.getLeadId())
//                .doctype(audioEntity.getDocumentType())
//                .documentName(documentName)
//                .status(audioEntity.getStatus())
//                .score((double)audioEntity.getOverallScore())
//                .description(audioEntity.getStatus()) // Or a more detailed description
//                .uploadedAt(audioEntity.getTimestamp())
//                .build();
//    }
//}

package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.config.DocumentTypeConfig;
import com.cars24.fraud_detection.data.dao.AudioDao;
import com.cars24.fraud_detection.data.dao.LeadDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.LeadEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.workflow.WorkflowInitiator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioServiceImpl implements AudioService {

    private static final String AUDIO_STORAGE_PATH = "src/main/resources/audio_storage";
    private final AudioDao audioDao;
    private final LeadDao leadDao;  // Inject LeadDao
    private final WorkflowInitiator workflowInitiator;
    private static final Logger logger = Logger.getLogger(AudioServiceImpl.class.getName());

    private final DocumentTypeConfig documentTypeConfig;

    @Override
    public AudioResponse processAudioRequest(AudioRequest audioRequest) throws JsonProcessingException, AudioProcessingException {
        logger.info("Received AudioRequest object: " + audioRequest);

        // 1. Validate Lead Existence
        LeadEntity lead = leadDao.findLeadById(audioRequest.getLeadId())
                .orElseThrow(() -> new AudioProcessingException("Lead not found with ID: " + audioRequest.getLeadId()));

        MultipartFile file = audioRequest.getAudioFile();

        // 2. Save the audio file
        // String filePath = saveAudio(file);
        String filePath = saveAudio(file, audioRequest.getUuid().toString()); // Pass correct UUID

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String uuid = fileName.replace(".mp3", ""); // Extract UUID from filename

        // 3. Process Audio using Workflow
        AudioResponse audioResponse = workflowInitiator.processAudio(audioRequest);

        // 4. Create AudioEntity
        AudioEntity audioEntity = new AudioEntity();
        audioEntity.setId(audioRequest.getUuid()); // Use UUID
        audioEntity.setLeadId(audioRequest.getLeadId());
        audioEntity.setAgentId(audioRequest.getAgentId());
        audioEntity.setDocumentType(audioRequest.getDocumentType());
        audioEntity.setLlmExtraction(audioResponse.getLlmExtraction());
        audioEntity.setTranscript(audioResponse.getTranscript());
        audioEntity.setReferenceName(audioResponse.getReferenceName());
        audioEntity.setSubjectName(audioResponse.getSubjectName());
        audioEntity.setSubjectAddress(audioResponse.getSubjectAddress());
        audioEntity.setRelationToSubject(audioResponse.getRelationToSubject());
        audioEntity.setSubjectOccupation(audioResponse.getSubjectOccupation());
        audioEntity.setOverallScore(audioResponse.getOverallScore());
        audioEntity.setExplanation(audioResponse.getExplanation());
        audioEntity.setFieldByFieldScores(audioResponse.getFieldByFieldScores());
        audioEntity.setAudioAnalysis(audioResponse.getAudioAnalysis());
        audioEntity.setStatus(audioResponse.getStatus());
        audioEntity.setTimestamp(LocalDateTime.now());

        // 5. Save AudioEntity
        audioDao.saveAudio(audioEntity);
        logger.info("Saved AudioEntity object to database with ID: " + audioEntity.getId());

        // 6. Return response with UUID
        audioResponse.setId(audioEntity.getId()); // Set the generated UUID
        audioResponse.setLeadId(audioEntity.getLeadId());
       // audioResponse.setAgentId(audioEntity.getAgentId());
        audioResponse.setDocumentType(audioEntity.getDocumentType());
        audioResponse.setLlmExtraction(audioEntity.getLlmExtraction());
        audioResponse.setTranscript(audioEntity.getTranscript());
        audioResponse.setReferenceName(audioEntity.getReferenceName());
        audioResponse.setSubjectName(audioEntity.getSubjectName());
        audioResponse.setSubjectAddress(audioEntity.getSubjectAddress());
        audioResponse.setRelationToSubject(audioEntity.getRelationToSubject());
        audioResponse.setSubjectOccupation(audioEntity.getSubjectOccupation());
        audioResponse.setOverallScore(audioEntity.getOverallScore());
        audioResponse.setExplanation(audioEntity.getExplanation());
        audioResponse.setFieldByFieldScores(audioEntity.getFieldByFieldScores());
        audioResponse.setAudioAnalysis(audioEntity.getAudioAnalysis());
        audioResponse.setStatus(audioEntity.getStatus());
       // audioResponse.setTimestamp(LocalDateTime.now());
        return audioResponse;
    }

    @Override
    public AudioResponse getAudioResults(String audioId) throws AudioProcessingException {
        logger.info("Fetching audio analysis");

        Optional<AudioEntity> audioEntityOpt = audioDao.getAudioById(audioId);
        if (audioEntityOpt.isEmpty()) {
            logger.warning("Audio analysis not found");
            throw new AudioProcessingException("Audio analysis not found for the provided ID");
        }

        AudioEntity audioEntity = audioEntityOpt.get();
        logger.info("Fetched audio entity successfully");

        AudioResponse response = new AudioResponse();
        response.setId(audioEntity.getId());
        response.setLeadId(audioEntity.getLeadId());
        response.setDocumentType(audioEntity.getDocumentType());
        response.setLlmExtraction(audioEntity.getLlmExtraction());
        response.setTranscript(audioEntity.getTranscript());
        response.setReferenceName(audioEntity.getReferenceName());
        response.setSubjectName(audioEntity.getSubjectName());
        response.setSubjectAddress(audioEntity.getSubjectAddress());
        response.setRelationToSubject(audioEntity.getRelationToSubject());
        response.setSubjectOccupation(audioEntity.getSubjectOccupation());
        response.setOverallScore(audioEntity.getOverallScore());
        response.setExplanation(audioEntity.getExplanation());
        response.setFieldByFieldScores(audioEntity.getFieldByFieldScores());
        response.setAudioAnalysis(audioEntity.getAudioAnalysis());
        response.setStatus(audioEntity.getStatus());

        return response;
    }

    @Override
    public List<AudioEntity> getAudiosByLeadId(String leadId) {
        return audioDao.findByLeadId(leadId);
    }

    @Override
    public List<String> getRecentAudios(String leadId, int limit) {
        log.info("Fetching recent audio UUIDs for lead ID: {} with limit: {}", leadId, limit);

        List<AudioEntity> audios = audioDao.getRecentAudiosByLeadId(leadId, limit);

        log.info("Total audios fetched: " + audios.size());

        return audios.stream()
                .map(AudioEntity::getId)
                .collect(Collectors.toList());
    }
    @Override
    public ResponseEntity<FileSystemResource> getAudioFile(String audioId){
        try {
            // Construct the file path
            Path filePath = Paths.get(AUDIO_STORAGE_PATH, audioId + ".mp3");
            File audioFile = filePath.toFile();

            if (!audioFile.exists()) {
//                log.warn("Audio file not found for id: {}", id);
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(audioFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg")); // Set content type to audio/mpeg
            headers.setContentLength(audioFile.length());
            headers.setContentDispositionFormData("attachment", audioId + ".mp3"); // Optional:  Suggest a filename


            return new ResponseEntity<>(resource, headers, HttpStatus.OK);


        } catch (Exception e) {
//            log.error("Error retrieving audio file for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public InsightsEntity getAudioInsights(String audioId) throws AudioProcessingException {
        AudioEntity audioEntity = audioDao.getAudioById(audioId)
                .orElseThrow(() -> new AudioProcessingException("Audio not found for ID: " + audioId));

        String documentName = documentTypeConfig.getMapping().get(audioEntity.getDocumentType());

        return InsightsEntity.builder()
                .leadId(audioEntity.getLeadId())
                .doctype(audioEntity.getDocumentType())
                .documentName(documentName)
                .status(audioEntity.getStatus())
                .score((double)audioEntity.getOverallScore())
                .description(audioEntity.getStatus()) // Or a more detailed description
                .uploadedAt(audioEntity.getTimestamp())
                .build();
    }

//    private String saveAudio(MultipartFile file) throws AudioProcessingException {
//        try {
//            // Check if file is empty
//            if (file.isEmpty()) {
//                logger.severe("Failed to save audio file: File is empty");
//                throw new AudioProcessingException("Failed to store audio file: File is empty");
//            }
//
//            // Ensure the storage directory exists
//            Path storagePath = Paths.get(AUDIO_STORAGE_PATH);
//            Files.createDirectories(storagePath);
//
//
//            // Generate a unique file name
//            String uniqueFileName = audioRequest.getUuid().toString() + ".mp3";
//
//            // Properly construct path using Path API
//            Path destinationFile = storagePath.resolve(uniqueFileName);
//
//            logger.info("Attempting to save file to: " + destinationFile.toString());
//
//            // Save the file to disk
//            Files.copy(file.getInputStream(), destinationFile);
//
//            logger.info("Audio file saved successfully to " + destinationFile.toString());
//
//            return destinationFile.toString();
//        } catch (IOException e) {
//            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
//            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
//        }
//    }

    public String saveAudio(MultipartFile file, String uuid) throws AudioProcessingException {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                logger.severe("Failed to save audio file: File is empty");
                throw new AudioProcessingException("Failed to store audio file: File is empty");
            }

            // Ensure the storage directory exists
            Path storagePath = Paths.get(AUDIO_STORAGE_PATH);
            Files.createDirectories(storagePath);

            // Use the correct UUID from audioRequest
            String uniqueFileName = uuid + ".mp3";

            // Properly construct path using Path API
            Path destinationFile = storagePath.resolve(uniqueFileName);

            logger.info("Attempting to save file to: " + destinationFile.toString());

            // Save the file to disk
            Files.copy(file.getInputStream(), destinationFile);

            logger.info("Audio file saved successfully to " + destinationFile.toString());

            return destinationFile.toString();
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
        }
    }

}